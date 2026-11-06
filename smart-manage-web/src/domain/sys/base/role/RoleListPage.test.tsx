// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { beforeEach, afterEach, expect, it, vi } from 'vitest';
import RoleListPage from './RoleListPage';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import type { RoleDefaultVO, RoleListVO } from './types';

const mocks = vi.hoisted(() => ({
  confirm: vi.fn(),
  feedback: { success: vi.fn(), warning: vi.fn(), fromError: vi.fn() },
}));
vi.mock('./api', () => ({ roleApi: { defaultRole: vi.fn(), setDefault: vi.fn() } }));
vi.mock('./useRoleDeleteMutation', () => ({
  useRoleDeleteMutation: () => ({ mutateAsync: vi.fn() }),
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => mocks.confirm,
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/registry/componentRegistry', () => ({
  getRegisteredTabTitle: () => '角色',
}));
vi.mock('@/domain/common/page/list/useListPageQuery', () => ({
  useListPageQuery: () => ({
    records: [{ id: 'new', number: 'NEW', name: '新角色', description: '' }],
    query: { isLoading: false, refetch: vi.fn() },
    onRefresh: vi.fn(),
  }),
}));
vi.mock('@/domain/common/page/list/ListPage', () => ({
  default: (props: {
    toolbarActions: Array<{
      key?: string;
      label?: string;
      disabled?: boolean;
      onClick?: () => void;
    }>;
    notice?: { title: ReactNode };
    columns: Array<{
      key?: string;
      render?: (value: undefined, record: RoleListVO) => ReactNode;
    }>;
    dataSource: RoleListVO[];
    onSelectChange: (keys: string[]) => void;
  }) => (
    <>
      {props.notice && <div role="note">{props.notice.title}</div>}
      {props.dataSource.map((record) => (
        <div key={record.id} data-role-id={record.id}>
          {props.columns
            .find((column) => column.key === 'defaultRole')
            ?.render?.(undefined, record)}
        </div>
      ))}
      <button onClick={() => props.onSelectChange(['new'])}>选中角色</button>
      {props.toolbarActions
        .filter((action) => action.key)
        .map((action) => (
          <button key={action.key} disabled={action.disabled} onClick={action.onClick}>
            {action.label}
          </button>
        ))}
    </>
  ),
}));

let root: Root;
let container: HTMLDivElement;
let client: QueryClient;
let configuration: RoleDefaultVO;
beforeEach(() => {
  vi.clearAllMocks();
  configuration = { role: { id: 'old', number: 'OLD', name: '旧角色' }, version: 7 };
  client = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: Infinity } } });
  vi.mocked(roleApi.defaultRole).mockImplementation(async () => configuration);
  vi.mocked(roleApi.setDefault).mockResolvedValue('');
  mocks.confirm.mockResolvedValue(false);
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  container.remove();
});
async function render() {
  client.setQueryData(roleQueryKeys.defaultRole(), configuration);
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <RoleListPage appNumber="sys" tabKey="role" componentKey="role" title="角色" active />
      </QueryClientProvider>,
    ),
  );
}
async function click(label: string) {
  const button = [...container.querySelectorAll('button')].find(
    (item) => item.textContent === label,
  );
  expect(button).toBeTruthy();
  await act(async () => button!.click());
}
it('requires one selection and waits for confirmation before switching using the captured version', async () => {
  await render();
  expect(
    [...container.querySelectorAll('button')].find((item) => item.textContent === '设为默认角色')
      ?.disabled,
  ).toBe(true);
  await click('选中角色');
  await click('设为默认角色');
  expect(roleApi.setDefault).not.toHaveBeenCalled();
  const confirmation = mocks.confirm.mock.calls[0]![0];
  expect(confirmation.description).toContain('OLD — 旧角色');
  expect(confirmation.description).toContain('NEW — 新角色');
  // 弹框打开后的查询更新不得悄悄升级用户确认的配置版本。
  client.setQueryData(roleQueryKeys.defaultRole(), { role: null, version: 8 });
  await act(async () => confirmation.onConfirm());
  expect(roleApi.setDefault).toHaveBeenCalledWith({
    roleId: 'new',
    previousRoleId: 'old',
    version: 7,
  });
});
it('cancels only the inherited default role', async () => {
  configuration = { role: { id: 'new', number: 'NEW', name: '新角色' }, version: 4 };
  await render();
  const marker = container.querySelector('[data-role-id="new"] .sm-role-default-check');
  expect(marker?.getAttribute('title')).toBe('默认角色');
  expect(container.querySelector('[role="note"]')).toBeNull();
  await click('选中角色');
  await click('取消默认角色');
  await act(async () => mocks.confirm.mock.calls[0]![0].onConfirm());
  expect(roleApi.setDefault).toHaveBeenCalledWith({
    roleId: null,
    previousRoleId: 'new',
    version: 4,
  });
});
it('sets the first default role without requiring an existing one', async () => {
  configuration = { role: null, version: 0 };
  await render();
  expect(container.querySelector('[role="note"]')).toBeNull();
  expect(container.querySelector('[data-role-id="new"]')?.textContent).toBe('');
  expect(container.querySelector('.sm-role-default-check')).toBeNull();
  await click('选中角色');
  await click('设为默认角色');
  await act(async () => mocks.confirm.mock.calls[0]![0].onConfirm());
  expect(roleApi.setDefault).toHaveBeenCalledWith({
    roleId: 'new',
    previousRoleId: null,
    version: 0,
  });
});
it('keeps failed commands rejected and reports no success', async () => {
  const failure = new Error('并发冲突');
  vi.mocked(roleApi.setDefault).mockRejectedValue(failure);
  await render();
  await click('选中角色');
  await click('设为默认角色');
  await act(async () => {
    await expect(mocks.confirm.mock.calls[0]![0].onConfirm()).rejects.toThrow('并发冲突');
  });
  expect(mocks.feedback.fromError).toHaveBeenCalledWith(failure);
  expect(mocks.feedback.success).not.toHaveBeenCalled();
});
