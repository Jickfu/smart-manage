// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { OperationConfirmProvider } from '@/domain/common/component/OperationConfirmProvider';
import SqlConsolePage from './SqlConsolePage';
import { sqlApi } from './api';
import type { SqlExecutionResult } from './types';

const selection = vi.hoisted(() => ({ text: '' }));
const feedback = vi.hoisted(() => ({ warning: vi.fn(), fromError: vi.fn() }));
vi.mock('./api', () => ({ sqlApi: { execute: vi.fn() } }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => feedback,
}));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({
    can: (permission: string) => permission.endsWith(':execute'),
    loading: false,
  }),
}));
vi.mock('@/domain/common/page/EditPageShell', () => ({
  EditPageShell: ({ actions, children }: { actions: ReactNode; children: ReactNode }) => (
    <>
      {actions}
      {children}
    </>
  ),
}));
vi.mock('./SqlCatalogBrowser', () => ({ default: () => null }));
// 只替换编辑器的布局与选择区；真实页面、Mutation、确认框和 Ant Design 按钮参与生命周期验证。
vi.mock('./SqlEditor', async () => {
  const { forwardRef, useImperativeHandle } = await import('react');
  return {
    default: forwardRef(
      ({ value, onChange }: { value: string; onChange: (value: string) => void }, ref) => {
        useImperativeHandle(ref, () => ({ getExecutableSql: () => selection.text }));
        return (
          <textarea
            aria-label="SQL"
            value={value}
            onChange={(event) => onChange(event.target.value)}
          />
        );
      },
    ),
  };
});

let container: HTMLDivElement;
let root: Root;
let queryClient: QueryClient;
const result: SqlExecutionResult = {
  type: 'DML',
  rowCount: 1,
  executeDuration: 5,
  truncated: false,
  statementCount: 1,
};

beforeEach(async () => {
  vi.clearAllMocks();
  selection.text = '';
  installJsdomBrowserStubs();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  queryClient = new QueryClient({ defaultOptions: { mutations: { retry: false } } });
  await act(async () =>
    root.render(
      <QueryClientProvider client={queryClient}>
        <OperationConfirmProvider>
          <SqlConsolePage appNumber="monitor" componentKey="sql" tabKey="sql" title="SQL" active />
        </OperationConfirmProvider>
      </QueryClientProvider>,
    ),
  );
});
afterEach(async () => {
  await act(async () => root.unmount());
  queryClient.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

function button(label: string) {
  const found = [...document.querySelectorAll('button')].find(
    (element) => element.textContent?.replace(/\s/g, '') === label,
  );
  if (!found) throw new Error(`缺少按钮：${label}`);
  return found;
}
async function enterSql(sql: string) {
  selection.text = sql;
  const editor = container.querySelector('textarea')!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype, 'value')!.set!.call(editor, sql);
    editor.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

it('写入确认等待请求、阻止重复提交与关闭，成功后才结束确认', async () => {
  let complete!: (value: SqlExecutionResult) => void;
  vi.mocked(sqlApi.execute).mockImplementation(
    () =>
      new Promise((resolve) => {
        complete = resolve;
      }),
  );
  await enterSql('UPDATE example SET value = 1');
  await act(async () => button('执行').click());
  expect(sqlApi.execute).not.toHaveBeenCalled();
  await act(async () => button('确认执行').click());
  expect(sqlApi.execute).toHaveBeenCalledTimes(1);
  expect(document.querySelector('[role="dialog"]')).not.toBeNull();
  expect(button('取消').disabled).toBe(true);
  expect(document.querySelector<HTMLButtonElement>('.ant-modal-close')?.disabled).toBe(true);
  await act(async () => {
    button('确认执行').click();
    button('取消').click();
    document
      .querySelector('.ant-modal-wrap')
      ?.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', keyCode: 27, bubbles: true }));
  });
  expect(sqlApi.execute).toHaveBeenCalledTimes(1);
  expect(document.querySelector('[role="dialog"]')).not.toBeNull();
  await act(async () => complete(result));
  await vi.waitFor(() => expect(document.querySelector('[role="dialog"]')).toBeNull());
});

it('写入失败保留 SQL 与确认框，允许明确重试', async () => {
  const error = new Error('模拟执行失败');
  vi.mocked(sqlApi.execute).mockRejectedValueOnce(error).mockResolvedValueOnce(result);
  await enterSql('UPDATE example SET value = 2');
  await act(async () => button('执行').click());
  await act(async () => button('确认执行').click());
  await vi.waitFor(() => expect(feedback.fromError).toHaveBeenCalledWith(error));
  expect(document.querySelector('[role="dialog"]')).not.toBeNull();
  expect(container.querySelector('textarea')?.value).toBe('UPDATE example SET value = 2');
  expect(button('取消').disabled).toBe(false);
  await act(async () => button('确认执行').click());
  await vi.waitFor(() => expect(document.querySelector('[role="dialog"]')).toBeNull());
  expect(sqlApi.execute).toHaveBeenCalledTimes(2);
});

it('选中查询仍直接执行，不增加确认流程', async () => {
  selection.text = 'SELECT 1';
  vi.mocked(sqlApi.execute).mockResolvedValue(result);
  await act(async () => button('执行').click());
  expect(sqlApi.execute).toHaveBeenCalledTimes(1);
  expect(document.querySelector('[role="dialog"]')).toBeNull();
});

it.each(['', '  \n  '])('无有效选区时不请求、不确认：%j', async (selectedText) => {
  selection.text = selectedText;
  await act(async () => button('执行').click());
  expect(sqlApi.execute).not.toHaveBeenCalled();
  expect(document.querySelector('[role="dialog"]')).toBeNull();
  expect(feedback.warning).toHaveBeenCalledWith('请先选中要执行的 SQL，再点击执行或按 Ctrl + E');
});

it('确认使用首次选区快照，不随后续编辑器选择改变', async () => {
  vi.mocked(sqlApi.execute).mockResolvedValue(result);
  await enterSql('UPDATE example SET value = 3');
  await act(async () => button('执行').click());
  selection.text = 'DELETE FROM example';
  await act(async () => button('确认执行').click());
  expect(vi.mocked(sqlApi.execute).mock.calls[0]?.[0]).toBe('UPDATE example SET value = 3');
});
