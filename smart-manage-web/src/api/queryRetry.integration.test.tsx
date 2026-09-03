// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import JobEditPage from '@/domain/sys/scheduler/job/JobEditPage';
import SlowSqlMonitorPage from '@/domain/sys/monitor/slowSql/SlowSqlMonitorPage';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import { ApiError } from './ApiError';

const mocks = vi.hoisted(() => ({
  job: { detail: vi.fn(), createNewData: vi.fn(), classes: vi.fn(), cronPreview: vi.fn() },
  slowSql: { instances: vi.fn(), snapshot: vi.fn() },
  feedback: { fromError: vi.fn(), success: vi.fn() },
}));
vi.mock('@/domain/sys/scheduler/job/api', () => ({ jobApi: mocks.job }));
vi.mock('@/domain/sys/monitor/slowSql/api', () => ({ slowSqlApi: mocks.slowSql }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => vi.fn(),
}));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true }),
}));
// 保留真实业务页面与 Query，替换不属于本测试的模板布局，直接操作页面提供的恢复入口。
vi.mock('@/domain/common/page/edit/EditPage', () => ({
  default: ({ onRetry }: { onRetry: () => void }) => <button onClick={onRetry}>重试</button>,
}));
vi.mock('@/domain/common/page/EditPageShell', () => ({
  EditPageShell: ({ onRetry }: { onRetry: () => void }) => <button onClick={onRetry}>重试</button>,
}));

let root: Root;
let container: HTMLDivElement;
let client: QueryClient;
const props: PageComponentProps = {
  title: '测试页面',
  appNumber: 'test',
  tabKey: 'test',
  componentKey: 'test',
  active: true,
};
const failure = new ApiError({ source: 'NETWORK', message: '' });
beforeEach(() => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  vi.resetAllMocks();
  container = document.createElement('div');
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  mocks.job.classes.mockRejectedValue(failure);
  mocks.job.createNewData.mockResolvedValue({});
  mocks.job.detail.mockResolvedValue({ id: 'job-1' });
  mocks.job.cronPreview.mockResolvedValue([]);
});
afterEach(async () => {
  await act(async () => root.unmount());
  client.clear();
  vi.unstubAllGlobals();
});
const settle = async () =>
  act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
async function render(page: ReactNode) {
  await act(async () =>
    root.render(<QueryClientProvider client={client}>{page}</QueryClientProvider>),
  );
  await settle();
}
async function retry() {
  await act(async () => container.querySelector('button')!.click());
  await settle();
}

it('retries a new job without calling detail(undefined)', async () => {
  await render(<JobEditPage {...props} operationType={OperationType.ADDNEW} />);
  await retry();
  expect(mocks.job.detail).not.toHaveBeenCalled();
  expect(mocks.job.classes).toHaveBeenCalledTimes(2);
  expect(mocks.job.createNewData).toHaveBeenCalledTimes(2);
});

it('retries an existing job without calling disabled creation defaults', async () => {
  await render(<JobEditPage {...props} operationType={OperationType.EDIT} billId="job-1" />);
  await retry();
  expect(mocks.job.createNewData).not.toHaveBeenCalled();
  expect(mocks.job.detail).toHaveBeenCalledTimes(2);
  expect(mocks.job.detail).toHaveBeenLastCalledWith('job-1');
});

it('does not retry a dependent snapshot without an instance and starts it after parent recovery', async () => {
  mocks.slowSql.instances.mockRejectedValue(failure);
  mocks.slowSql.snapshot.mockResolvedValue({ records: [] });
  await render(<SlowSqlMonitorPage {...props} />);
  await retry();
  expect(mocks.slowSql.snapshot).not.toHaveBeenCalled();
  expect(mocks.slowSql.instances).toHaveBeenCalledTimes(2);
  mocks.slowSql.instances.mockResolvedValue([{ instanceId: 'instance-1', current: true }]);
  await retry();
  await settle();
  expect(mocks.slowSql.snapshot).toHaveBeenCalledExactlyOnceWith('instance-1');
});
