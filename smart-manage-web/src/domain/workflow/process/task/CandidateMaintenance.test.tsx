// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { ConfigProvider } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { beforeEach, afterEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import CandidateMaintenance from './CandidateMaintenance';

const mocks = vi.hoisted(() => ({ post: vi.fn() }));
vi.mock('../api', () => ({ flowPost: mocks.post }));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true }),
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({ fromError: vi.fn(), success: vi.fn() }),
}));
vi.mock('@/domain/common/component/RefSelector', () => ({
  default: ({ value = [] }: { value?: { handlerName: string }[] }) => (
    <output data-testid="candidates">
      {value.map((candidate) => candidate.handlerName).join(',')}
    </output>
  ),
}));

let root: Root;
let container: HTMLDivElement;
let client: QueryClient;
beforeEach(() => {
  installJsdomBrowserStubs();
  vi.clearAllMocks();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  mocks.post.mockImplementation(async (path: string, data: { storageIds: string }) => {
    if (path === 'task/maintenance-target')
      return {
        id: '10',
        number: 'LV-1',
        tasks: [
          {
            id: '20',
            name: '部门审批',
            candidates: Array.from({ length: 201 }, (_, index) => String(index + 1)),
          },
        ],
      };
    return data.storageIds.split(',').map((storageId) => ({
      storageId,
      handlerCode: storageId,
      handlerName: `姓名${storageId.replace('sm:user:', '')}`,
    }));
  });
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function settle() {
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
}
function button(label: string) {
  return [...document.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === label,
  )!;
}
async function load() {
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <ConfigProvider theme={{ zeroRuntime: true }}>
          <CandidateMaintenance />
        </ConfigProvider>
      </QueryClientProvider>,
    ),
  );
  await act(async () => button('维护候选人').click());
  const input = document.querySelector<HTMLInputElement>('input[placeholder="流程实例 ID"]')!;
  await act(async () => {
    Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')!.set!.call(input, '10');
    input.dispatchEvent(new Event('input', { bubbles: true }));
  });
  await act(async () => button('读取任务').click());
  await settle();
}
it('超过 100 名候选人按接口上限分批回显，选中任务仍保留全部候选人姓名', async () => {
  await load();
  const requests = mocks.post.mock.calls.filter(([path]) => path === 'assignment/feedback');
  expect(requests.map(([, data]) => data.storageIds.split(',').length)).toEqual([100, 100, 1]);
  const selector = document.querySelector('[role="combobox"]')!;
  await act(async () => selector.dispatchEvent(new MouseEvent('mousedown', { bubbles: true })));
  await settle();
  const option = document.querySelector<HTMLElement>('.ant-select-item-option')!;
  await act(async () => option.click());
  const names = document.querySelector('[data-testid="candidates"]')!.textContent!.split(',');
  expect(names).toHaveLength(201);
  expect(names[200]).toBe('姓名201');
});
it('任一回显批次失败不会开放保存不完整的候选集合', async () => {
  const implementation = mocks.post.getMockImplementation()!;
  mocks.post.mockImplementation(async (path, data) => {
    if (path === 'assignment/feedback' && data.storageIds.startsWith('sm:user:101'))
      throw new Error('回显失败');
    return implementation(path, data);
  });
  await load();
  expect(button('保存变更')).toHaveProperty('disabled', true);
  expect(document.querySelector('[data-testid="candidates"]')).toBeNull();
});
