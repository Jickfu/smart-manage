// @vitest-environment jsdom
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import SingletonListPage from './SingletonListPage';
import { singletonApi } from './api';
import { singletonQueryKeys } from './queryKeys';
import type { SingletonTask } from './types';
const mocks = vi.hoisted(() => ({
  feedback: { success: vi.fn(), warning: vi.fn(), fromError: vi.fn() },
  confirm: vi.fn(),
}));
vi.mock('./api', () => ({ singletonApi: { listPage: vi.fn(), control: vi.fn() } }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => mocks.confirm,
}));
let container: HTMLDivElement;
let root: Root;
let client: QueryClient;
let task: SingletonTask;
beforeEach(() => {
  vi.clearAllMocks();
  installJsdomBrowserStubs();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({
    defaultOptions: { queries: { retry: false, staleTime: Infinity }, mutations: { retry: false } },
  });
  client.setQueryData(['current-user-permissions', 'sys:scheduler:singleton'], ['*']);
  task = {
    number: 'test:loop',
    name: '循环任务',
    appNumber: 'scheduler',
    description: '验证',
    controllable: true,
    available: true,
    requested: false,
    version: 3,
    state: 'STOPPED',
    generation: '0',
  };
  vi.mocked(singletonApi.listPage).mockImplementation(async () => ({
    total: 1,
    pageNum: 1,
    pageSize: 20,
    records: [task],
  }));
  vi.mocked(singletonApi.control).mockResolvedValue('');
  mocks.confirm.mockResolvedValue(false);
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function renderPage() {
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <SingletonListPage
          appNumber="scheduler"
          componentKey="sys/scheduler/singleton"
          tabKey="singleton"
          title="常驻任务"
          active={false}
        />
      </QueryClientProvider>,
    ),
  );
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 30));
  });
  const radio = container.querySelector<HTMLInputElement>('input[type="radio"]');
  expect(radio).toBeTruthy();
  await act(async () => radio!.click());
}
function button(label: string) {
  return [...container.querySelectorAll<HTMLButtonElement>('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === label,
  )!;
}
it('确认绑定原任务和版本，刷新不替换待确认命令；成功只报告运行期望', async () => {
  await renderPage();
  expect(button('启动').disabled).toBe(false);
  await act(async () => button('启动').click());
  expect(singletonApi.control).not.toHaveBeenCalled();
  const options = mocks.confirm.mock.calls[0]![0] as {
    description: string;
    onConfirm: () => Promise<unknown>;
  };
  expect(options.description).toContain('test:loop');
  task = { ...task, version: 4 };
  await act(async () => {
    await client.invalidateQueries({ queryKey: singletonQueryKeys.lists() });
  });
  await act(async () => {
    await options.onConfirm();
  });
  expect(singletonApi.control).toHaveBeenCalledWith({
    number: 'test:loop',
    version: 3,
    requested: true,
  });
  expect(mocks.feedback.success).toHaveBeenCalledWith('运行期望已更新，请查看实际运行状态');
});
it('停止收束和未知状态不能重新启动，未装配任务不能控制', async () => {
  task = { ...task, state: 'STOPPING', ownerInstanceId: 'node-a' };
  await renderPage();
  expect(button('启动').disabled).toBe(true);
  expect(button('停止').disabled).toBe(true);
  task = { ...task, state: 'UNKNOWN', requested: true, available: false };
  await act(async () => {
    await client.invalidateQueries({ queryKey: singletonQueryKeys.lists() });
  });
  expect(button('启动').disabled).toBe(true);
  expect(button('停止').disabled).toBe(true);
});
