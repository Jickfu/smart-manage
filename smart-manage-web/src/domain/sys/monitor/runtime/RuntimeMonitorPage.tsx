import { lazy, Suspense, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Descriptions,
  Progress,
  Segmented,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { EChartsCoreOption } from 'echarts/core';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { runtimeMonitorApi } from './api';
import { runtimeMonitorQueryKeys as keys } from './queryKeys';
import type { MonitorHost, RuntimeSnapshot } from './types';
import './runtimeMonitor.css';

const SmChart = lazy(() => import('@/domain/common/chart/SmChart'));
const ranges = [
  { label: '1 小时', value: '1h' },
  { label: '6 小时', value: '6h' },
  { label: '24 小时', value: '24h' },
  { label: '7 天', value: '7d' },
];
const percent = (value?: number) => Math.max(0, Math.min(100, (value ?? 0) * 100));
const ratio = (used: number, total: number) => (total > 0 ? percent(used / total) : 0);
const bytes = (value?: number) => {
  const safe = value ?? 0;
  if (safe <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const index = Math.min(Math.floor(Math.log(safe) / Math.log(1024)), 4);
  return `${(safe / 1024 ** index).toFixed(index ? 1 : 0)} ${units[index]}`;
};

export default function RuntimeMonitorPage({ active }: PageComponentProps) {
  const [instanceId, setInstanceId] = useState<string>();
  const [range, setRange] = useState('1h');
  const [scope, setScope] = useState<'HOST' | 'INSTANCE'>('INSTANCE');
  const topology = useQuery({
    queryKey: keys.topology(),
    queryFn: runtimeMonitorApi.topology,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const instances = useQuery({
    queryKey: keys.instances(),
    queryFn: runtimeMonitorApi.instances,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const selectedId =
    instanceId ??
    instances.data?.find((item) => item.current)?.instanceId ??
    instances.data?.[0]?.instanceId;
  const snapshot = useQuery({
    queryKey: keys.snapshot(selectedId),
    queryFn: () => runtimeMonitorApi.snapshot(selectedId),
    enabled: active && Boolean(selectedId),
    refetchInterval: active ? 10000 : false,
  });
  const scopeId = scope === 'HOST' ? snapshot.data?.hostId : selectedId;
  const history = useQuery({
    queryKey: keys.history(scope, scopeId ?? '', range),
    queryFn: () => runtimeMonitorApi.history(scope, scopeId!, range),
    enabled: active && Boolean(scopeId),
  });
  const chart = useMemo<EChartsCoreOption>(
    () => historyOption(scope, history.data ?? []),
    [history.data, scope],
  );
  return (
    <EditPageShell
      title="运行监控"
      loading={topology.isLoading || snapshot.isLoading}
      error={topology.error ?? snapshot.error}
      onRetry={() => {
        void topology.refetch();
        void snapshot.refetch();
      }}
      actions={
        <Space size={10}>
          <Select
            value={selectedId}
            placeholder="选择应用实例"
            options={(instances.data ?? []).map((item) => ({
              value: item.instanceId,
              label: `${item.instanceId} · ${item.hostId}${item.current ? '（当前）' : ''}`,
              disabled: false,
            }))}
            onChange={setInstanceId}
          />
          <Segmented
            value={scope}
            options={[
              { label: '实例趋势', value: 'INSTANCE' },
              { label: '主机趋势', value: 'HOST' },
            ]}
            onChange={(value) => setScope(value as 'HOST' | 'INSTANCE')}
          />
          <Select value={range} options={ranges} onChange={setRange} />
          <Button
            type="primary"
            loading={snapshot.isFetching}
            onClick={() => {
              void snapshot.refetch();
              void topology.refetch();
              void history.refetch();
            }}
          >
            立即刷新
          </Button>
        </Space>
      }
    >
      <div className="sm-runtime-monitor">
        <TopologyCard hosts={topology.data ?? []} />
        {snapshot.data && (
          <>
            <SnapshotSummary snapshot={snapshot.data} />
            <Card
              title={`${scope === 'HOST' ? '主机' : '实例'}历史趋势`}
              extra={
                <Typography.Text type="secondary">后台持续采样，历史不依赖页面打开</Typography.Text>
              }
            >
              <Suspense fallback={<div>正在加载图表</div>}>
                <SmChart option={chart} ariaLabel="运行监控历史趋势" />
              </Suspense>
            </Card>
            <FilesystemCard snapshot={snapshot.data} />
          </>
        )}
      </div>
    </EditPageShell>
  );
}

function TopologyCard({ hosts }: { hosts: MonitorHost[] }) {
  return (
    <Card
      title="运行拓扑"
      extra={<Typography.Text type="secondary">Host 1 — N Application Instance</Typography.Text>}
    >
      <Table
        size="small"
        pagination={false}
        rowKey="host_id"
        dataSource={hosts}
        expandable={{
          expandedRowRender: (host) => (
            <Table
              size="small"
              pagination={false}
              rowKey="instance_id"
              dataSource={host.instances}
              columns={[
                { title: '实例 ID', dataIndex: 'instance_id' },
                { title: '应用', dataIndex: 'application_name', width: 180 },
                { title: '版本', dataIndex: 'application_version', width: 140 },
                {
                  title: '状态',
                  dataIndex: 'online',
                  width: 120,
                  render: (online: boolean) => (
                    <Tag color={online ? 'success' : 'default'}>{online ? '在线' : '离线'}</Tag>
                  ),
                },
                { title: '最后发现', dataIndex: 'last_seen_time', width: 180 },
              ]}
            />
          ),
        }}
        columns={[
          { title: '主机', dataIndex: 'host_name' },
          { title: 'Host ID', dataIndex: 'host_id', width: 200 },
          {
            title: '操作系统',
            render: (_, host) => `${host.os_name ?? '-'} ${host.os_version ?? ''}`,
          },
          {
            title: '遥测状态',
            dataIndex: 'status',
            width: 180,
            render: (status: string) => (
              <Tag color={status === 'UP' ? 'success' : 'warning'}>
                {status === 'UP' ? '可用' : '主机遥测不可用'}
              </Tag>
            ),
          },
          { title: '实例数', width: 100, render: (_, host) => host.instances.length },
        ]}
      />
    </Card>
  );
}
function Metric({ title, value }: { title: string; value: number }) {
  return (
    <Card size="small">
      <Typography.Text type="secondary">{title}</Typography.Text>
      <Progress percent={Number(value.toFixed(1))} status={value >= 90 ? 'exception' : 'normal'} />
    </Card>
  );
}
function SnapshotSummary({ snapshot }: { snapshot: RuntimeSnapshot }) {
  return (
    <>
      <div className="sm-runtime-monitor-metrics">
        <Metric title="主机 CPU" value={percent(snapshot.cpu.systemUsage)} />
        <Metric title="进程 CPU" value={percent(snapshot.cpu.processUsage)} />
        <Metric
          title="物理内存"
          value={ratio(
            snapshot.memory.physicalTotal - snapshot.memory.physicalAvailable,
            snapshot.memory.physicalTotal,
          )}
        />
        <Metric title="JVM 堆" value={ratio(snapshot.memory.heapUsed, snapshot.memory.heapMax)} />
        <Metric
          title="连接池"
          value={ratio(snapshot.dataSource.active, snapshot.dataSource.maxActive)}
        />
      </div>
      <Card
        title={`${snapshot.instanceId} 实时快照`}
        extra={
          <Tag color={snapshot.health.status === 'UP' ? 'success' : 'error'}>
            {snapshot.health.status}
          </Tag>
        }
      >
        <Descriptions
          size="small"
          column={4}
          items={[
            { key: 'host', label: 'Host ID', children: snapshot.hostId },
            { key: 'sample', label: '采样时间', children: snapshot.sampleTime },
            {
              key: 'os',
              label: '操作系统',
              children: `${snapshot.os.name} ${snapshot.os.version}`,
            },
            { key: 'jvm', label: 'JVM', children: snapshot.runtime.vmName },
            {
              key: 'threads',
              label: '活动/阻塞线程',
              children: `${snapshot.threads.live} / ${snapshot.threads.stateCounts.BLOCKED ?? 0}`,
            },
            {
              key: 'http',
              label: 'HTTP 请求/5xx 速率',
              children: `${snapshot.http.requestRate?.toFixed(2) ?? '-'} / ${snapshot.http.serverErrorRate?.toFixed(2) ?? '-'} req/s`,
            },
            {
              key: 'latency',
              label: 'HTTP P95 / P99',
              children: `${snapshot.http.p95Ms?.toFixed(1) ?? '-'} / ${snapshot.http.p99Ms?.toFixed(1) ?? '-'} ms`,
            },
            {
              key: 'io',
              label: '磁盘读/写',
              children: `${bytes(snapshot.io.diskReadBytesPerSecond)}/s / ${bytes(snapshot.io.diskWriteBytesPerSecond)}/s`,
            },
          ]}
        />
      </Card>
    </>
  );
}
function FilesystemCard({ snapshot }: { snapshot: RuntimeSnapshot }) {
  return (
    <Card title="文件系统">
      <Table
        size="small"
        pagination={false}
        rowKey={(item) => `${item.name}:${item.mount}`}
        dataSource={snapshot.filesystems}
        columns={[
          { title: '挂载点', dataIndex: 'mount' },
          { title: '名称', dataIndex: 'name' },
          { title: '类型', dataIndex: 'type', width: 120 },
          { title: '已用', width: 140, render: (_, item) => bytes(item.used) },
          { title: '可用', width: 140, render: (_, item) => bytes(item.available) },
          {
            title: '使用率',
            width: 200,
            render: (_, item) => (
              <Progress
                percent={Number(ratio(item.used, item.total).toFixed(1))}
                status={ratio(item.used, item.total) >= 90 ? 'exception' : 'normal'}
              />
            ),
          },
        ]}
      />
    </Card>
  );
}
function historyOption(
  scope: 'HOST' | 'INSTANCE',
  points: Array<Record<string, string | number | null>>,
): EChartsCoreOption {
  const definitions =
    scope === 'HOST'
      ? [
          ['CPU %', 'cpu_usage', 100],
          ['内存 %', 'memory_used', null],
          ['磁盘读 B/s', 'disk_read_rate', null],
          ['网络收 B/s', 'network_receive_rate', null],
        ]
      : [
          ['进程 CPU %', 'process_cpu', 100],
          ['堆内存', 'heap_used', null],
          ['线程数', 'thread_count', null],
          ['HTTP P95 ms', 'http_p95_ms', null],
        ];
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: definitions.map((item) => item[0]) },
    grid: { left: 56, right: 24, top: 48, bottom: 32 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((item) => String(item.sample_time).replace('T', ' ').slice(0, 16)),
    },
    yAxis: { type: 'value' },
    series: definitions.map(([name, key, multiplier]) => ({
      name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: points.map((item) => {
        const value = Number(item[String(key)] ?? 0);
        return multiplier ? value * Number(multiplier) : value;
      }),
    })),
  };
}
