// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot, type Root } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import type { EChartsCoreOption } from 'echarts/core';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { installJsdomBrowserStubs } from '@/test/jsdomBrowserStubs';
import { ApiError } from '@/api/ApiError';
import SchedulerHome from '@/domain/sys/scheduler/home/SchedulerHome';
import { schedulerHomeApi } from '@/domain/sys/scheduler/home/api';
import RuntimeMonitorPage from '@/domain/sys/monitor/runtime/RuntimeMonitorPage';
import { runtimeMonitorApi } from '@/domain/sys/monitor/runtime/api';
import { runtimeMonitorQueryKeys } from '@/domain/sys/monitor/runtime/queryKeys';

const captured = vi.hoisted(() => new Map<string, EChartsCoreOption>());
// 替换图形绘制，保留真实页面、查询缓存、入口、弹框和表格，用以核对两种表示的数据来源。
vi.mock('./SmChart', () => ({
  default: ({ option, ariaLabel }: { option: EChartsCoreOption; ariaLabel: string }) => {
    captured.set(ariaLabel, option);
    return <div aria-label={ariaLabel} />;
  },
}));
vi.mock('@/domain/common/home/QuickLaunchCard', () => ({ default: () => null }));
vi.mock('@/domain/sys/scheduler/home/api', () => ({ schedulerHomeApi: { summary: vi.fn() } }));
vi.mock('@/domain/sys/monitor/runtime/api', () => ({
  runtimeMonitorApi: {
    topology: vi.fn(),
    instances: vi.fn(),
    instanceSnapshot: vi.fn(),
    hostSnapshot: vi.fn(),
    history: vi.fn(),
    retire: vi.fn(),
  },
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => vi.fn(),
}));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({ success: vi.fn(), fromError: vi.fn() }),
}));
vi.mock('@/domain/common/page/access/usePermissionAccess', () => ({
  usePermissionAccess: () => ({ can: () => true }),
}));
vi.mock('@/domain/common/page/EditPageShell', () => ({
  EditPageShell: ({ actions, children }: { actions: ReactNode; children: ReactNode }) => (
    <>
      {actions}
      {children}
    </>
  ),
}));

let container: HTMLDivElement;
let root: Root;
let queryClient: QueryClient;
const summary = {
  totalJobs: 1,
  enabledJobs: 1,
  pausedJobs: 0,
  runningExecutions: 0,
  todayExecutions: 12,
  todayFailedExecutions: 0,
  trends: [{ date: '2026-11-04', success: 123456789, failed: 0, skipped: 2 }],
};
const points = [
  {
    sampleTime: '2026-11-04T00:00:01.123456789+08:00',
    processCpuUsage: 0.123456789,
    heapUsage: 0,
    threadCount: 123456789,
    p95Ms: 98765.4321,
  },
];
beforeEach(() => {
  vi.clearAllMocks();
  captured.clear();
  installJsdomBrowserStubs();
  container = document.createElement('div');
  document.body.append(container);
  root = createRoot(container);
  queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
  });
  vi.mocked(schedulerHomeApi.summary).mockResolvedValue(summary);
  vi.mocked(runtimeMonitorApi.topology).mockResolvedValue([]);
  vi.mocked(runtimeMonitorApi.instances).mockResolvedValue([
    {
      instanceId: 'instance-test',
      hostId: 'host-test',
      applicationName: 'test',
      applicationVersion: '1',
      lifecycle: 'ACTIVE',
      online: true,
      current: true,
      startTime: '',
      lastSeenTime: '',
    },
  ]);
  vi.mocked(runtimeMonitorApi.instanceSnapshot).mockResolvedValue({ status: 'UNAVAILABLE' });
  vi.mocked(runtimeMonitorApi.hostSnapshot).mockResolvedValue({ status: 'UNAVAILABLE' });
  vi.mocked(runtimeMonitorApi.history).mockResolvedValue(points);
});
afterEach(async () => {
  await act(async () => root.unmount());
  queryClient.clear();
  container.remove();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});
async function settle() {
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 40));
  });
}
async function mount(page: ReactNode) {
  await act(async () =>
    root.render(
      <ConfigProvider locale={zhCN}>
        <QueryClientProvider client={queryClient}>{page}</QueryClientProvider>
      </ConfigProvider>,
    ),
  );
  await settle();
  await settle();
}
function button(label: string) {
  const target = [...document.querySelectorAll('button')].find(
    (node) => node.textContent?.replace(/\s/g, '') === label,
  );
  if (!target) throw new Error(`缺少按钮：${label}`);
  return target;
}
function dialog() {
  return document.querySelector('[role="dialog"]')!;
}
function seriesData(label: string) {
  return (captured.get(label)?.series as Array<{ data: unknown[] }>).map((series) => series.data);
}
async function mountRuntime() {
  await mount(
    <RuntimeMonitorPage
      appNumber="monitor"
      componentKey="runtime"
      tabKey="runtime"
      title="运行监控"
      active
    />,
  );
}

it('调度表和图表复用已有统计结果，完整显示日期、次数和零，缓存更新后同步变化', async () => {
  await mount(<SchedulerHome />);
  await act(async () => button('查看数据').click());
  expect(schedulerHomeApi.summary).toHaveBeenCalledTimes(1);
  expect(dialog().textContent).toContain('2026-11-04');
  expect(dialog().textContent).toContain('成功（次）');
  expect(dialog().textContent).toContain('123456789');
  expect(seriesData('近七天任务执行趋势')).toEqual([[123456789], [0], [2]]);
  await act(async () =>
    queryClient.setQueryData(['sys', 'scheduler', 'home', 'summary'], {
      ...summary,
      trends: [{ date: '2026-11-04', success: 41, failed: 3, skipped: 0 }],
    }),
  );
  await settle();
  expect(dialog().textContent).toContain('41');
  expect(seriesData('近七天任务执行趋势')).toEqual([[41], [3], [0]]);
  expect(schedulerHomeApi.summary).toHaveBeenCalledTimes(1);
});

it('监控表保留完整时间和原始比例，图表只转换百分比，缺失不替换成零', async () => {
  await mountRuntime();
  await act(async () => button('查看数据').click());
  expect(runtimeMonitorApi.history).toHaveBeenCalledTimes(1);
  expect(dialog().textContent).toContain(points[0]!.sampleTime);
  expect(dialog().textContent).toContain('0.123456789');
  expect(dialog().textContent).toContain('1=100%');
  const cells = [...dialog().querySelectorAll('tbody td')].map((node) => node.textContent);
  expect(cells).toContain('0');
  expect(cells).toContain('—');
  expect(seriesData('JVM 利用率')).toEqual([[0.123456789 * 100], [0], [null]]);
  vi.mocked(runtimeMonitorApi.history).mockRejectedValue(new Error('temporary failure'));
  await act(async () => {
    await queryClient.refetchQueries({
      queryKey: runtimeMonitorQueryKeys.history('INSTANCE', 'instance-test', '1h'),
    });
  });
  await settle();
  expect(dialog().textContent).toContain('上次成功加载的数据');
  expect(dialog().textContent).toContain('0.123456789');
});

it('历史访问被拒绝时，已经打开的数据表不继续显示缓存原值', async () => {
  await mountRuntime();
  await act(async () => button('查看数据').click());
  vi.mocked(runtimeMonitorApi.history).mockRejectedValue(
    new ApiError({ source: 'HTTP', message: '无权限', httpStatus: 403 }),
  );
  await act(async () => {
    await queryClient.refetchQueries({
      queryKey: runtimeMonitorQueryKeys.history('INSTANCE', 'instance-test', '1h'),
    });
  });
  await settle();
  expect(dialog().textContent).not.toContain('0.123456789');
  expect(dialog().querySelector('table')).toBeNull();
});

it('空统计结果允许查看并显示空态，不虚构一行零值', async () => {
  vi.mocked(schedulerHomeApi.summary).mockResolvedValue({ ...summary, trends: [] });
  await mount(<SchedulerHome />);
  await act(async () => button('查看数据').click());
  expect(dialog().textContent).toContain('暂无数据');
  expect(dialog().textContent).not.toContain('2026-11-04');
  expect(seriesData('近七天任务执行趋势')).toEqual([[], [], []]);
});

it('延迟数据入口保留毫秒原值及当前对象范围', async () => {
  await mountRuntime();
  const entries = [...document.querySelectorAll('button')].filter(
    (node) => node.textContent === '查看数据',
  );
  await act(async () => entries[2]!.click());
  expect(dialog().textContent).toContain('P95（ms）');
  expect(dialog().textContent).toContain('98765.4321');
  expect(dialog().textContent).toContain('instance-test');
  expect(dialog().textContent).toContain('1 小时');
  expect(seriesData('HTTP 延迟')).toEqual([[98765.4321], [null]]);
  expect(runtimeMonitorApi.history).toHaveBeenCalledTimes(1);
});
