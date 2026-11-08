// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { OperationConfirmProvider } from '@/domain/common/component/OperationConfirmProvider';
import WeakPasswordListPage from '@/domain/sys/base/weakPassword/WeakPasswordListPage';
import { weakPasswordApi } from '@/domain/sys/base/weakPassword/api';
import { weakPasswordQueryKeys } from '@/domain/sys/base/weakPassword/queryKeys';
import ScriptListPage from '@/domain/sys/monitor/script/ScriptListPage';
import { scriptApi } from '@/domain/sys/monitor/script/api';
import { scriptQueryKeys } from '@/domain/sys/monitor/script/queryKeys';

vi.mock('@/domain/sys/base/weakPassword/api', () => ({
  weakPasswordApi: { listPage: vi.fn(), delete: vi.fn() },
}));
vi.mock('@/domain/sys/monitor/script/api', () => ({
  scriptApi: { listPage: vi.fn(), delete: vi.fn() },
}));
vi.mock('@/domain/sys/base/weakPassword/WeakPasswordEditPage', () => ({ default: () => null }));
vi.mock('@/domain/common/page/tab/usePageTabTitle', () => ({ usePageTabTitle: () => undefined }));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true, loading: false }),
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({ success: vi.fn(), warning: vi.fn(), fromError: vi.fn() }),
}));

let container: HTMLDivElement;
let root: Root;
let queryClient: QueryClient;
beforeEach(async () => {
  vi.clearAllMocks();
  installJsdomBrowserStubs();
  vi.mocked(weakPasswordApi.listPage).mockResolvedValue({
    records: [
      { id: 'word-old', version: 3, word: '<img src=x onerror=alert(1)>', description: '' },
    ],
    total: 1,
    pageNum: 1,
    pageSize: 20,
  });
  vi.mocked(weakPasswordApi.delete).mockResolvedValue('word-old');
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  await act(async () => {
    root.render(
      <QueryClientProvider client={queryClient}>
        <OperationConfirmProvider>
          <WeakPasswordListPage
            appNumber="base"
            componentKey="weakPassword"
            tabKey="weakPassword"
            title="弱口令"
            active
          />
        </OperationConfirmProvider>
      </QueryClientProvider>,
    );
  });
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 30));
  });
});
afterEach(async () => {
  await act(async () => root.unmount());
  queryClient.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
function button(label: string) {
  const matches = [...document.querySelectorAll('button')].filter(
    (element) => element.textContent?.replace(/\s/g, '') === label,
  );
  const found = matches.at(-1);
  if (!found) throw new Error(`缺少按钮：${label}`);
  return found;
}
async function openConfirmation() {
  const checkbox = container.querySelector('tbody input[type="checkbox"]') as HTMLInputElement;
  expect(checkbox).not.toBeNull();
  await act(async () => checkbox.click());
  await act(async () => button('删除').click());
}

it('显示具体词条和后果，按文本渲染特殊字符；取消不发删除命令', async () => {
  await openConfirmation();
  const dialog = document.querySelector('[role="dialog"]')!;
  expect(dialog.textContent).toContain('词条："<img src=x onerror=alert(1)>"');
  expect(dialog.textContent).toContain('将不再参与弱口令拦截');
  expect(dialog.querySelector('img[src="x"]')).toBeNull();
  await act(async () => button('取消').click());
  expect(weakPasswordApi.delete).not.toHaveBeenCalled();
});

it('后台列表替换后，确认仍删除文案对应的原对象及版本', async () => {
  await openConfirmation();
  await act(async () => {
    queryClient.setQueriesData(
      { queryKey: weakPasswordQueryKeys.all },
      {
        records: [{ id: 'word-new', version: 9, word: 'new-word' }],
        total: 1,
      },
    );
  });
  expect(document.querySelector('[role="dialog"]')?.textContent).toContain(
    '<img src=x onerror=alert(1)>',
  );
  await act(async () => button('删除').click());
  expect(vi.mocked(weakPasswordApi.delete).mock.calls[0]?.[0]).toEqual({
    id: 'word-old',
    version: 3,
  });
});

it('脚本删除使用打开确认时的 ID 与版本，不使用后台刷新后的记录', async () => {
  vi.mocked(scriptApi.listPage).mockResolvedValue({
    records: [
      {
        id: 'script-old',
        version: 2,
        number: 'OLD',
        name: '旧脚本',
        description: '',
        createTime: '',
        updateTime: '',
      },
    ],
    total: 1,
    pageNum: 1,
    pageSize: 20,
  });
  await act(async () =>
    root.render(
      <QueryClientProvider client={queryClient}>
        <OperationConfirmProvider>
          <ScriptListPage
            appNumber="monitor"
            componentKey="script"
            tabKey="script"
            title="脚本"
            active
          />
        </OperationConfirmProvider>
      </QueryClientProvider>,
    ),
  );
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 30));
  });
  await openConfirmation();
  await act(async () => {
    queryClient.setQueriesData(
      { queryKey: scriptQueryKeys.all },
      { records: [{ id: 'script-old', version: 8, number: 'NEW', name: '新脚本' }], total: 1 },
    );
  });
  expect(document.querySelector('[role="dialog"]')?.textContent).toContain('OLD — 旧脚本');
  await act(async () => button('确认删除').click());
  expect(scriptApi.delete).toHaveBeenCalledWith('script-old', 2);
});
