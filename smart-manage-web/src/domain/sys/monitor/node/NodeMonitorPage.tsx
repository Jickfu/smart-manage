import { lazy, Suspense, useMemo, useState } from 'react';
import { Button, Card, Descriptions, Progress, Select, Space, Table, Tag, Typography } from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { EChartsCoreOption } from 'echarts/core';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { nodeMonitorApi } from './api';
import { nodeMonitorQueryKeys } from './queryKeys';
import type { NodeSnapshot } from './types';
import './nodeMonitor.css';

const SmChart = lazy(() => import('@/domain/common/chart/SmChart'));
const MAX_HISTORY_POINTS = 30;
const REFRESH_INTERVAL_OPTIONS = [
  { value: 0, label: '手动刷新' },
  { value: 10000, label: '每 10 秒刷新' },
  { value: 30000, label: '每 30 秒刷新' },
  { value: 60000, label: '每 1 分钟刷新' },
];

interface HistoryPoint {
  time: string;
  systemCpu: number;
  processCpu: number;
  heap: number;
  threads: number;
}

function percent(value?: number): number {
  return Math.max(0, Math.min(100, (value ?? 0) * 100));
}

function ratio(used: number, total: number): number {
  return total > 0 ? Math.max(0, Math.min(100, (used / total) * 100)) : 0;
}

function formatBytes(value: number): string {
  if (value <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const unitIndex = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1);
  return `${(value / 1024 ** unitIndex).toFixed(unitIndex === 0 ? 0 : 1)} ${units[unitIndex]}`;
}

function formatDuration(value: number): string {
  const totalMinutes = Math.floor(value / 60000);
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const minutes = totalMinutes % 60;
  return `${days}天 ${hours}小时 ${minutes}分钟`;
}

function progressStatus(value: number): 'normal' | 'exception' {
  return value >= 90 ? 'exception' : 'normal';
}

export default function NodeMonitorPage({ active }: PageComponentProps) {
  const [history, setHistory] = useState<HistoryPoint[]>([]);
  const [selectedInstanceId, setSelectedInstanceId] = useState<string>();
  const [refreshInterval, setRefreshInterval] = useState(0);
  const instancesQuery = useQuery({
    queryKey: nodeMonitorQueryKeys.instances(),
    queryFn: nodeMonitorApi.instances,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const effectiveInstanceId =
    selectedInstanceId ?? instancesQuery.data?.find((instance) => instance.current)?.instanceId;
  const snapshotQuery = useQuery({
    queryKey: nodeMonitorQueryKeys.snapshot(effectiveInstanceId),
    queryFn: async () => {
      const nextSnapshot = await nodeMonitorApi.snapshot(effectiveInstanceId);
      setHistory((current) => [
        ...current.slice(-(MAX_HISTORY_POINTS - 1)),
        {
          time: nextSnapshot.sampleTime.slice(11),
          systemCpu: percent(nextSnapshot.cpu.systemUsage),
          processCpu: percent(nextSnapshot.cpu.processUsage),
          heap: ratio(nextSnapshot.memory.heapUsed, nextSnapshot.memory.heapMax),
          threads: nextSnapshot.threads.live,
        },
      ]);
      return nextSnapshot;
    },
    enabled: active && Boolean(effectiveInstanceId),
    refetchInterval: active && effectiveInstanceId && refreshInterval > 0 ? refreshInterval : false,
    refetchIntervalInBackground: false,
  });
  const snapshot = snapshotQuery.data;

  const chartOption = useMemo<EChartsCoreOption>(
    () => ({
      tooltip: { trigger: 'axis' },
      legend: { data: ['系统 CPU', '进程 CPU', '堆内存', '线程数'] },
      grid: { left: 48, right: 48, top: 48, bottom: 32 },
      xAxis: { type: 'category', boundaryGap: false, data: history.map((item) => item.time) },
      yAxis: [
        { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
        { type: 'value', min: 0 },
      ],
      series: [
        {
          name: '系统 CPU',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: history.map((item) => item.systemCpu),
        },
        {
          name: '进程 CPU',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: history.map((item) => item.processCpu),
        },
        {
          name: '堆内存',
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: history.map((item) => item.heap),
        },
        {
          name: '线程数',
          type: 'line',
          smooth: true,
          showSymbol: false,
          yAxisIndex: 1,
          data: history.map((item) => item.threads),
        },
      ],
    }),
    [history],
  );

  return (
    <EditPageShell
      title="服务状态"
      loading={snapshotQuery.isLoading}
      error={snapshotQuery.error}
      onRetry={() => void snapshotQuery.refetch()}
      actions={
        <Space size={10}>
          <Select
            value={effectiveInstanceId}
            placeholder="选择在线实例"
            options={(instancesQuery.data ?? []).map((instance) => ({
              value: instance.instanceId,
              label: `${instance.instanceId}${instance.current ? '（当前）' : ''}`,
            }))}
            onChange={(instanceId) => {
              setSelectedInstanceId(instanceId);
              setHistory([]);
            }}
          />
          <Select
            value={refreshInterval}
            options={REFRESH_INTERVAL_OPTIONS}
            onChange={setRefreshInterval}
          />
          <Button loading={snapshotQuery.isFetching} onClick={() => void snapshotQuery.refetch()}>
            立即刷新
          </Button>
        </Space>
      }
    >
      {snapshot && (
        <div className="sm-node-monitor">
          <div className="sm-node-monitor-heading">
            <div>
              <Typography.Title level={4}>监控实例：{snapshot.instanceId}</Typography.Title>
              <Typography.Text type="secondary">
                当前展示所选在线实例的实时运行快照，采样时间 {snapshot.sampleTime}
              </Typography.Text>
            </div>
            <Tag color={snapshot.health.status === 'UP' ? 'success' : 'error'}>
              {snapshot.health.status}
            </Tag>
          </div>

          <div className="sm-node-monitor-metrics">
            <MetricCard title="系统 CPU" value={percent(snapshot.cpu.systemUsage)} />
            <MetricCard title="进程 CPU" value={percent(snapshot.cpu.processUsage)} />
            <MetricCard
              title="堆内存"
              value={ratio(snapshot.memory.heapUsed, snapshot.memory.heapMax)}
            />
            <MetricCard title="磁盘" value={ratio(snapshot.disk.used, snapshot.disk.total)} />
            <MetricCard
              title="连接池"
              value={ratio(snapshot.dataSource.active, snapshot.dataSource.maxActive)}
            />
          </div>

          <Card title="当前页面最近 30 次采样" className="sm-node-monitor-card">
            <Suspense fallback={<div className="sm-node-monitor-chart-loading">正在加载图表</div>}>
              <SmChart option={chartOption} ariaLabel="CPU、堆内存和线程数历史趋势" />
            </Suspense>
          </Card>

          <div className="sm-node-monitor-grid">
            <Card title="运行环境" className="sm-node-monitor-card">
              <Descriptions size="small" column={2} items={runtimeItems(snapshot)} />
            </Card>
            <Card title="资源明细" className="sm-node-monitor-card">
              <Descriptions size="small" column={2} items={resourceItems(snapshot)} />
            </Card>
          </div>

          <div className="sm-node-monitor-grid">
            <Card title="数据源连接池" className="sm-node-monitor-card">
              <Descriptions size="small" column={3} items={dataSourceItems(snapshot)} />
            </Card>
            <Card title="健康组件" className="sm-node-monitor-card">
              <Space wrap size={10}>
                {snapshot.health.components.map((component) => (
                  <Tag key={component.name} color={component.status === 'UP' ? 'success' : 'error'}>
                    {component.name} · {component.status}
                  </Tag>
                ))}
              </Space>
            </Card>
          </div>

          <Card title="垃圾回收器" className="sm-node-monitor-card">
            <Table
              size="small"
              pagination={false}
              rowKey="name"
              dataSource={snapshot.gc}
              columns={[
                { title: '名称', dataIndex: 'name' },
                { title: '累计次数', dataIndex: 'collectionCount', width: 140 },
                { title: '累计耗时（ms）', dataIndex: 'collectionTimeMs', width: 160 },
              ]}
            />
          </Card>
        </div>
      )}
    </EditPageShell>
  );
}

function MetricCard({ title, value }: { title: string; value: number }) {
  return (
    <Card size="small" className="sm-node-monitor-metric-card">
      <Typography.Text type="secondary">{title}</Typography.Text>
      <Progress
        type="dashboard"
        percent={Number(value.toFixed(1))}
        status={progressStatus(value)}
        size={112}
      />
    </Card>
  );
}

function runtimeItems(snapshot: NodeSnapshot) {
  return [
    { key: 'javaVersion', label: 'Java 版本', children: snapshot.runtime.javaVersion },
    { key: 'javaVendor', label: 'Java 厂商', children: snapshot.runtime.javaVendor },
    { key: 'vmName', label: 'JVM', children: snapshot.runtime.vmName },
    { key: 'processors', label: '可用处理器', children: snapshot.runtime.processors },
    { key: 'os', label: '操作系统', children: `${snapshot.os.name} ${snapshot.os.version}` },
    { key: 'arch', label: '架构', children: snapshot.os.arch },
    { key: 'startTime', label: '启动时间', children: snapshot.runtime.startTime },
    { key: 'uptime', label: '运行时长', children: formatDuration(snapshot.runtime.uptimeMs) },
  ];
}

function resourceItems(snapshot: NodeSnapshot) {
  return [
    {
      key: 'heap',
      label: '堆内存',
      children: `${formatBytes(snapshot.memory.heapUsed)} / ${formatBytes(snapshot.memory.heapMax)}`,
    },
    {
      key: 'nonHeap',
      label: '非堆内存',
      children: `${formatBytes(snapshot.memory.nonHeapUsed)} / ${formatBytes(snapshot.memory.nonHeapCommitted)}`,
    },
    {
      key: 'physical',
      label: '物理内存可用',
      children: `${formatBytes(snapshot.memory.physicalAvailable)} / ${formatBytes(snapshot.memory.physicalTotal)}`,
    },
    {
      key: 'disk',
      label: `磁盘 ${snapshot.disk.mount ?? ''}`,
      children: `${formatBytes(snapshot.disk.available)} 可用 / ${formatBytes(snapshot.disk.total)}`,
    },
    { key: 'threads', label: '活动线程', children: snapshot.threads.live },
    { key: 'threadPeak', label: '线程峰值', children: snapshot.threads.peak },
    { key: 'daemon', label: '守护线程', children: snapshot.threads.daemon },
    { key: 'blocked', label: '阻塞线程', children: snapshot.threads.stateCounts.BLOCKED ?? 0 },
  ];
}

function dataSourceItems(snapshot: NodeSnapshot) {
  return [
    { key: 'active', label: '活跃连接', children: snapshot.dataSource.active },
    { key: 'idle', label: '空闲连接', children: snapshot.dataSource.idle },
    { key: 'max', label: '最大连接', children: snapshot.dataSource.maxActive },
    { key: 'waiting', label: '等待线程', children: snapshot.dataSource.waiting },
    { key: 'connectCount', label: '累计连接', children: snapshot.dataSource.connectCount },
    { key: 'errorCount', label: '累计错误', children: snapshot.dataSource.errorCount },
  ];
}
