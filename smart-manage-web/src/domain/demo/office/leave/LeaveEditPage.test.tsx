// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { ConfigProvider } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { beforeEach, afterEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { OperationType } from '@/domain/common/page/types';
import LeaveEditPage from './LeaveEditPage';
import type { LeaveDetail } from './api';

const mocks = vi.hoisted(() => ({
  detail: vi.fn(),
  save: vi.fn(),
  submit: vi.fn(),
  workspace: {
    registerBeforeClose: vi.fn(),
    unregisterBeforeClose: vi.fn(),
    replaceContentTab: vi.fn(),
  },
}));
vi.mock('./api', () => ({ leaveApi: mocks }));
vi.mock('@/api/user', () => ({
  getCurrentPermissions: async () => ['demo:office:leave:save', 'demo:office:leave:submit'],
}));
vi.mock('@/stores/workbench', () => ({ useWorkbenchStore: { getState: () => mocks.workspace } }));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => async () => true,
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({ fromError: vi.fn(), success: vi.fn() }),
}));

let root: Root;
let container: HTMLDivElement;
let client: QueryClient;
let detail: LeaveDetail;
beforeEach(() => {
  installJsdomBrowserStubs();
  vi.clearAllMocks();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  detail = {
    id: '10',
    version: 1,
    number: 'LV-1',
    clientKey: 'client-1',
    orgId: '1',
    applicantId: '2',
    bizDate: '2026-09-26',
    leaveType: 'PERSONAL',
    startTime: '2026-09-27 09:00:00',
    endTime: '2026-09-27 18:00:00',
    days: 1,
    reason: '回归验收',
    billStatus: 'B',
    currentInstanceId: '100',
    attachments: [],
    retainedAttachmentIds: [],
  };
  mocks.detail.mockImplementation(async () => detail);
  mocks.submit.mockResolvedValue('10');
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
    await new Promise((resolve) => setTimeout(resolve, 50));
  });
}
async function render() {
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <ConfigProvider theme={{ zeroRuntime: true }}>
          <LeaveEditPage
            appNumber="office"
            tabKey="leave-10"
            componentKey="demo/office/leave/edit"
            title="请假申请"
            billId="10"
            operationType={OperationType.VIEW}
            active
          />
        </ConfigProvider>
      </QueryClientProvider>,
    ),
  );
  await settle();
}
function button(label: string) {
  return [...container.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === label,
  );
}
async function refresh(value: Partial<LeaveDetail>) {
  detail = { ...detail, ...value };
  await act(async () => {
    await client.invalidateQueries({ queryKey: ['demo', 'leave', '10'] });
  });
  await settle();
}
it.each(['WITHDRAWN', 'REJECTED'])(
  '原查看页签在 %s 后恢复编辑与提交，审批通过仍只读',
  async (outcome) => {
    await render();
    expect(container.querySelector('textarea')).toHaveProperty('disabled', true);
    await refresh({ version: 2, billStatus: 'A', lastOutcome: outcome });
    expect(container.querySelector('textarea')).toHaveProperty('disabled', false);
    expect(button('保存')).toHaveProperty('disabled', false);
    expect(button('提交')).toHaveProperty('disabled', false);
    await refresh({ version: 3, billStatus: 'C', lastOutcome: 'APPROVED' });
    expect(container.querySelector('textarea')).toHaveProperty('disabled', true);
  },
);
it('同一命令失败重试复用 requestId，撤回后重新提交使用新 requestId', async () => {
  detail = { ...detail, billStatus: 'A', lastOutcome: 'WITHDRAWN' };
  mocks.submit.mockRejectedValueOnce(new Error('网络失败'));
  await render();
  const submit = async () => {
    await act(async () => button('提交')!.click());
    await settle();
  };
  await submit();
  await submit();
  expect(mocks.submit).toHaveBeenCalledTimes(2);
  const first = mocks.submit.mock.calls[0]![0];
  expect(first.requestId).toBeTruthy();
  expect(mocks.submit.mock.calls[1]![0].requestId).toBe(first.requestId);
  await refresh({ version: 2, billStatus: 'B', lastOutcome: undefined });
  await refresh({ version: 3, billStatus: 'A', lastOutcome: 'WITHDRAWN' });
  await submit();
  expect(mocks.submit).toHaveBeenCalledTimes(3);
  expect(mocks.submit.mock.calls[2]![0].requestId).not.toBe(first.requestId);
});
