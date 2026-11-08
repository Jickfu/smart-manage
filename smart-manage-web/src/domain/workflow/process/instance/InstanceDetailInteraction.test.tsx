// @vitest-environment jsdom
import { act } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { ConfigProvider } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { InstanceAdminDetail } from '../api';
import InstanceDetailPage from './InstanceDetailPage';

const mocks = vi.hoisted(() => ({
  detail: vi.fn(),
  post: vi.fn(),
  confirm: vi.fn(),
  feedback: {
    fromError: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  },
  removeContentTab: vi.fn(),
}));

vi.mock('../api', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../api')>();
  return {
    ...actual,
    flowPost: mocks.post,
    workflowApi: { ...actual.workflowApi, instanceAdminDetail: mocks.detail },
  };
});
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true, loading: false }),
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => mocks.confirm,
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/stores/workbench', () => ({
  useWorkbenchStore: { getState: () => ({ removeContentTab: mocks.removeContentTab }) },
}));
vi.mock('../task/CandidateMaintenance', () => ({ default: () => <span>维护候选人</span> }));

let container: HTMLDivElement;
let root: Root;
let client: QueryClient;
const props: PageComponentProps = {
  appNumber: 'process',
  componentKey: 'workflow/process/instance/detail',
  tabKey: 'instance-detail-test',
  title: '流程实例',
  billId: '100',
  active: true,
};

function detail(active: boolean, script = false): InstanceAdminDetail {
  return {
    id: '100',
    businessType: 'demo/office/leave',
    businessId: '200',
    number: 'LV-100',
    orgId: '300',
    applicantId: '10',
    actorNames: { '10': '申请人', '20': '审批人' },
    candidateChanges: [],
    operations: [],
    run: {
      id: '100',
      applicantId: '10',
      state: 'APPROVING',
      active,
      variables: { days: 2 },
      tasks: [
        {
          id: '400',
          nodeCode: script ? 'script-risk' : 'manager-approval',
          name: script ? '风险计算' : '主管审批',
          candidates: script ? [] : ['20'],
          script,
        },
      ],
      nodeTargets: [
        { code: 'start', name: '开始', script: false },
        { code: 'manager-approval', name: '主管审批', script: false },
        { code: 'finance-approval', name: '财务审批', script: false },
      ],
      history: [
        {
          id: '500',
          nodeCode: 'manager-approval',
          nodeName: '主管审批',
          actorId: '20',
          action: 'APPROVED',
          time: '2026-10-02T16:01:02.123Z',
        },
      ],
    },
  };
}

beforeEach(() => {
  installJsdomBrowserStubs();
  vi.clearAllMocks();
  mocks.detail.mockResolvedValue(detail(true));
  mocks.post.mockResolvedValue({});
  mocks.confirm.mockResolvedValue(true);
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
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

async function renderPage() {
  await act(async () => {
    root.render(
      <QueryClientProvider client={client}>
        <ConfigProvider theme={{ zeroRuntime: true }}>
          <InstanceDetailPage {...props} />
        </ConfigProvider>
      </QueryClientProvider>,
    );
  });
  await settle();
}

function button(label: string) {
  return [...document.querySelectorAll<HTMLButtonElement>('button')].find(
    (element) => element.textContent?.replace(/\s/g, '') === label,
  );
}

function formItem(label: string) {
  return [...document.querySelectorAll<HTMLElement>('.ant-form-item-label')]
    .find((element) => element.textContent?.includes(label))
    ?.closest<HTMLElement>('.ant-form-item');
}

async function setText(label: string, value: string) {
  const input = formItem(label)?.querySelector<HTMLInputElement | HTMLTextAreaElement>(
    'input, textarea',
  );
  expect(input).toBeTruthy();
  await act(async () => {
    const prototype =
      input instanceof HTMLTextAreaElement
        ? HTMLTextAreaElement.prototype
        : HTMLInputElement.prototype;
    Object.getOwnPropertyDescriptor(prototype, 'value')!.set!.call(input, value);
    input!.dispatchEvent(new Event('input', { bubbles: true }));
    input!.dispatchEvent(new Event('change', { bubbles: true }));
  });
  await settle();
}

async function selectOption(label: string, optionText: string) {
  const item = formItem(label);
  const selector = item?.querySelector<HTMLInputElement>('.ant-select-input');
  if (!selector) {
    throw new Error(
      `未找到选择器 ${label}，当前标签：${[
        ...document.querySelectorAll<HTMLElement>('.ant-form-item-label'),
      ]
        .map((element) => element.textContent)
        .join('|')}`,
    );
  }
  expect(selector).toBeTruthy();
  await act(async () => {
    selector.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    selector.click();
  });
  await settle();
  const option = [...document.querySelectorAll<HTMLElement>('.ant-select-item-option')].find(
    (element) => element.textContent === optionText,
  );
  expect(option).toBeTruthy();
  await act(async () => option!.click());
  await settle();
}

async function selectRadio(label: string) {
  const radio = [...document.querySelectorAll<HTMLElement>('.ant-radio-wrapper')].find(
    (element) => element.textContent === label,
  );
  expect(radio).toBeTruthy();
  await act(async () => radio!.click());
  await settle();
}

it('流转轨迹按管理端时区显示跨日的引擎历史', async () => {
  await renderPage();

  expect(container.textContent).toContain('2026-10-02T16:01:02.123Z');
});

it('活动实例只开放可执行命令，失败重试复用同一请求标识', async () => {
  mocks.post.mockRejectedValueOnce(new Error('temporary failure')).mockResolvedValueOnce({});
  await renderPage();

  expect(button('挂起')).toBeTruthy();
  expect(button('节点调整')).toBeTruthy();
  expect(button('终止')).toBeTruthy();
  expect(button('恢复')).toBeUndefined();
  expect(button('修改变量')).toBeUndefined();

  await act(async () => button('挂起')!.click());
  await setText('操作原因', '维护前暂时挂起');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenCalledTimes(1);
  expect(mocks.post).toHaveBeenLastCalledWith(
    'instance/suspend',
    expect.objectContaining({
      instanceId: '100',
      reason: '维护前暂时挂起',
      requestId: expect.any(String),
    }),
  );
  const firstRequestId = mocks.post.mock.calls[0]![1].requestId;

  await act(async () => button('确定')!.click());
  await settle();
  expect(mocks.post).toHaveBeenCalledTimes(2);
  expect(mocks.post.mock.calls[1]![1].requestId).toBe(firstRequestId);
  expect(mocks.feedback.fromError).toHaveBeenCalledTimes(1);
});

it('挂起实例支持变量修改并规范化删除项，成功后刷新相关管理查询', async () => {
  mocks.detail.mockResolvedValue(detail(false));
  const invalidate = vi.spyOn(client, 'invalidateQueries');
  await renderPage();

  expect(button('恢复')).toBeTruthy();
  expect(button('修改变量')).toBeTruthy();
  expect(button('挂起')).toBeUndefined();

  await act(async () => button('修改变量')!.click());
  await setText('新增或修改变量', '{"riskLevel":"HIGH","days":3}');
  await setText('删除变量名', 'obsolete,\n temporary ,,');
  await setText('操作原因', '修正后续网关变量');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenCalledWith(
    'instance/variables',
    expect.objectContaining({
      instanceId: '100',
      reason: '修正后续网关变量',
      changes: { riskLevel: 'HIGH', days: 3 },
      removals: ['obsolete', 'temporary'],
      requestId: expect.any(String),
    }),
  );
  expect(invalidate).toHaveBeenCalledWith({
    queryKey: ['workflow', 'instance-admin-detail', '100'],
  });
  expect(invalidate).toHaveBeenCalledWith({ queryKey: ['workflow', 'instances'] });
  expect(invalidate).toHaveBeenCalledWith({ queryKey: ['workflow', 'task-monitor'] });
  expect(invalidate).toHaveBeenCalledWith({ queryKey: ['workflow', 'flow-logs'] });
});

it('挂起实例可以提交恢复命令', async () => {
  mocks.detail.mockResolvedValue(detail(false));
  await renderPage();

  await act(async () => button('恢复')!.click());
  await setText('操作原因', '维护完成，恢复审批');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenCalledWith(
    'instance/resume',
    expect.objectContaining({
      instanceId: '100',
      reason: '维护完成，恢复审批',
      requestId: expect.any(String),
    }),
  );
});

it('节点调整按前进和退回方向提交对应目标', async () => {
  await renderPage();

  await act(async () => button('节点调整')!.click());
  await settle();
  await selectOption('目标节点', '财务审批（finance-approval）');
  await setText('操作原因', '跳过无需处理的中间节点');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenLastCalledWith(
    'instance/jump',
    expect.objectContaining({
      instanceId: '100',
      taskId: '400',
      action: 'JUMP',
      targetNodeCode: 'finance-approval',
      reason: '跳过无需处理的中间节点',
      requestId: expect.any(String),
    }),
  );

  await act(async () => button('节点调整')!.click());
  await settle();
  await selectRadio('退回节点');
  await selectOption('目标节点', '主管审批（manager-approval）');
  await setText('操作原因', '退回已流转节点复核');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenLastCalledWith(
    'instance/jump',
    expect.objectContaining({
      instanceId: '100',
      taskId: '400',
      action: 'RETURN',
      targetNodeCode: 'manager-approval',
      reason: '退回已流转节点复核',
      requestId: expect.any(String),
    }),
  );
});

it('失败脚本只在挂起时开放重试，并提交当前脚本任务', async () => {
  mocks.detail.mockResolvedValue(detail(false, true));
  await renderPage();

  expect(button('重试脚本')).toBeTruthy();
  await act(async () => button('重试脚本')!.click());
  await setText('操作原因', '已修正流程变量，重新执行脚本');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.post).toHaveBeenCalledWith(
    'instance/script-retry',
    expect.objectContaining({
      instanceId: '100',
      taskId: '400',
      reason: '已修正流程变量，重新执行脚本',
      requestId: expect.any(String),
    }),
  );
});

it('终止命令必须经过破坏性确认，取消确认不发送请求', async () => {
  mocks.confirm.mockResolvedValue(false);
  await renderPage();

  await act(async () => button('终止')!.click());
  await setText('操作原因', '业务已经线下关闭');
  await act(async () => button('确定')!.click());
  await settle();

  expect(mocks.confirm).toHaveBeenCalledWith(
    expect.objectContaining({
      type: 'destructive',
      confirmText: '终止流程',
      description: expect.stringContaining('LV-100'),
      onConfirm: expect.any(Function),
    }),
  );
  expect(mocks.post).not.toHaveBeenCalled();
});

it('终止失败后可在同一确认流程中以原请求标识重试', async () => {
  mocks.post.mockRejectedValueOnce(new Error('连接中断')).mockResolvedValueOnce(undefined);
  await renderPage();

  await act(async () => button('终止')!.click());
  await setText('操作原因', '业务已经线下关闭');
  await act(async () => button('确定')!.click());
  const { onConfirm } = mocks.confirm.mock.calls[0]![0] as { onConfirm: () => Promise<unknown> };

  await act(async () => {
    await expect(onConfirm()).rejects.toThrow('连接中断');
  });
  await act(async () => {
    await onConfirm();
  });

  expect(mocks.post).toHaveBeenCalledTimes(2);
  expect(mocks.post.mock.calls[0]).toEqual(mocks.post.mock.calls[1]);
  expect(mocks.post).toHaveBeenLastCalledWith(
    'instance/terminate',
    expect.objectContaining({ instanceId: '100', reason: '业务已经线下关闭' }),
  );
});

it('管理记录将终止命令显示为中文动作', async () => {
  const value = detail(false);
  value.operations = [
    {
      id: '600',
      action: 'TERMINATE',
      operatorId: '20',
      reason: '业务已经线下关闭',
      time: '2026-10-03 09:30:00',
    },
  ];
  mocks.detail.mockResolvedValue(value);
  await renderPage();

  await act(async () =>
    [...document.querySelectorAll<HTMLElement>('[role="tab"]')]
      .find((element) => element.textContent === '管理记录')!
      .click(),
  );
  await settle();

  expect(container.textContent).toContain('终止');
  expect(container.textContent).not.toContain('TERMINATE');
});
