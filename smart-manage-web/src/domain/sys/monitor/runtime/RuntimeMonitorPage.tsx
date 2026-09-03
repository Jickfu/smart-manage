import { lazy, Suspense, useMemo, useState } from 'react';
import {
  Button,
  Alert,
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
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { EChartsCoreOption } from 'echarts/core';
import { EditPageShell } from '@/domain/common/page/edit/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { runtimeMonitorApi } from './api';
import { runtimeMonitorQueryKeys as keys } from './queryKeys';
import type { HistoryPoint, HostSnapshot, InstanceSnapshot, MonitorHost } from './types';
import {
  monitorBytes as bytes,
  monitorHealthPresentation,
  monitorHistoryValues,
  monitorPercent as percent,
  monitorRatio as ratio,
} from './formatters';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { runtimeMonitorAccess } from './permissions';
import './runtimeMonitor.css';

const SmChart = lazy(() => import('@/domain/common/chart/SmChart'));
const ranges = [
  { label: '1 小时', value: '1h' },
  { label: '6 小时', value: '6h' },
  { label: '24 小时', value: '24h' },
  { label: '7 天', value: '7d' },
];

export default function RuntimeMonitorPage({ active }: PageComponentProps) {
  const [instanceId, setInstanceId] = useState<string>();
  const [range, setRange] = useState('1h');
  const [scope, setScope] = useState<'HOST' | 'INSTANCE'>('INSTANCE');
  const [selectedLifecycleInstanceId, setSelectedLifecycleInstanceId] = useState<string>();
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  const { can } = usePermissionAccess(runtimeMonitorAccess.prefix);
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
  const instanceSnapshot = useQuery({
    queryKey: keys.instanceSnapshot(selectedId),
    queryFn: () => runtimeMonitorApi.instanceSnapshot(selectedId),
    enabled: active && Boolean(selectedId),
    refetchInterval: active ? 10000 : false,
  });
  const selectedInstance = instances.data?.find((item) => item.instanceId === selectedId);
  const selectedHostId = selectedInstance?.hostId;
  const hostSnapshot = useQuery({
    queryKey: keys.hostSnapshot(selectedHostId),
    queryFn: () => runtimeMonitorApi.hostSnapshot(selectedHostId!),
    enabled: active && Boolean(selectedHostId),
    refetchInterval: active ? 10000 : false,
  });
  const scopeId = scope === 'HOST' ? selectedHostId : selectedId;
  const history = useQuery({
    queryKey: keys.history(scope, scopeId ?? '', range),
    queryFn: () => runtimeMonitorApi.history(scope, scopeId!, range),
    enabled: active && Boolean(scopeId),
  });
  const retire = useMutation({
    mutationFn: runtimeMonitorApi.retire,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: keys.topology() });
      setSelectedLifecycleInstanceId(undefined);
      feedback.success('实例已退役');
    },
    onError: (error) => feedback.fromError(error, '实例退役失败'),
  });
  const charts = useMemo(() => historyOptions(scope, history.data ?? []), [history.data, scope]);
  return (
    <EditPageShell
      title="运行监控"
      loading={topology.isLoading || instances.isLoading}
      error={topology.error ?? instances.error}
      onRetry={() => {
        void topology.refetch();
        void instanceSnapshot.refetch();
        void hostSnapshot.refetch();
      }}
      actions={
        <Space size={10}>
          <Select
            value={selectedId}
            placeholder="选择应用实例"
            options={(instances.data ?? []).map((item) => ({
              value: item.instanceId,
              label: `${item.instanceId} · ${item.hostId}（${item.lifecycle === 'RETIRED' ? '已退役' : item.online ? '在线' : '离线'}${item.current ? '，当前' : ''}）`,
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
            loading={instanceSnapshot.isFetching || hostSnapshot.isFetching}
            onClick={() => {
              void instanceSnapshot.refetch();
              void hostSnapshot.refetch();
              void topology.refetch();
              void history.refetch();
            }}
          >
            立即刷新
          </Button>
          <Button
            type="primary"
            danger
            disabled={!selectedLifecycleInstanceId || !can(runtimeMonitorAccess.permissions.manage)}
            onClick={() =>
              selectedLifecycleInstanceId &&
              void confirmOperation({
                type: 'warning',
                title: '退役应用实例',
                description: `退役 ${selectedLifecycleInstanceId} 后将不再评估离线告警。`,
                confirmText: '确认退役',
                onConfirm: () => retire.mutateAsync(selectedLifecycleInstanceId),
              })
            }
          >
            退役实例
          </Button>
        </Space>
      }
    >
      <div className="sm-runtime-monitor">
        <TopologyCard
          hosts={topology.data ?? []}
          selectedInstanceId={selectedLifecycleInstanceId}
          onSelectInstance={setSelectedLifecycleInstanceId}
        />
        {selectedInstance && (
          <Alert
            showIcon
            type={
              selectedInstance.lifecycle === 'RETIRED'
                ? 'info'
                : selectedInstance.online
                  ? 'success'
                  : 'warning'
            }
            title={
              selectedInstance.lifecycle === 'RETIRED'
                ? '当前实例已退役，仍可查看历史趋势'
                : selectedInstance.online
                  ? '当前实例在线'
                  : '当前实例离线，实时遥测不可用，历史趋势仍可查询'
            }
          />
        )}
        {selectedHostId && hostSnapshot.data?.status === 'UNAVAILABLE' && (
          <Alert
            showIcon
            type="warning"
            title="当前主机遥测不可用"
            description="这不等同于主机宕机；仍可查看该主机已经持久化的历史趋势。"
          />
        )}
        {instanceSnapshot.data?.snapshot && (
          <SnapshotSummary
            host={hostSnapshot.data?.snapshot}
            instance={instanceSnapshot.data.snapshot}
          />
        )}
        {history.error && (
          <Alert
            showIcon
            type="error"
            title="历史趋势加载失败"
            description={history.error.message}
          />
        )}
        {charts.map((chart) => (
          <Card key={chart.title} title={chart.title} loading={history.isLoading}>
            <Suspense fallback={<div>正在加载图表</div>}>
              <SmChart option={chart.option} ariaLabel={chart.title} />
            </Suspense>
          </Card>
        ))}
        {hostSnapshot.data?.snapshot && <FilesystemCard snapshot={hostSnapshot.data.snapshot} />}
      </div>
    </EditPageShell>
  );
}

function TopologyCard({
  hosts,
  selectedInstanceId,
  onSelectInstance,
}: {
  hosts: MonitorHost[];
  selectedInstanceId?: string;
  onSelectInstance: (id?: string) => void;
}) {
  return (
    <Card
      title="运行拓扑"
      extra={<Typography.Text type="secondary">Host 1 — N Application Instance</Typography.Text>}
    >
      <Table
        size="small"
        pagination={false}
        rowKey="hostId"
        dataSource={hosts}
        expandable={{
          expandedRowRender: (host) => (
            <Table
              size="small"
              pagination={false}
              rowKey="instanceId"
              dataSource={host.instances}
              rowSelection={{
                type: 'checkbox',
                selectedRowKeys: selectedInstanceId ? [selectedInstanceId] : [],
                getCheckboxProps: (item) => ({
                  disabled: item.lifecycle === 'RETIRED' || item.online,
                }),
                onChange: (keys) =>
                  onSelectInstance(keys.length === 1 ? String(keys[0]) : undefined),
              }}
              columns={[
                { title: '实例 ID', dataIndex: 'instanceId' },
                { title: '应用', dataIndex: 'applicationName', width: 180 },
                { title: '版本', dataIndex: 'applicationVersion', width: 140 },
                { title: '生命周期', dataIndex: 'lifecycle', width: 110 },
                {
                  title: '状态',
                  dataIndex: 'online',
                  width: 120,
                  render: (online: boolean) => (
                    <Tag color={online ? 'success' : 'default'}>{online ? '在线' : '离线'}</Tag>
                  ),
                },
                { title: '最后发现', dataIndex: 'lastSeenTime', width: 180 },
              ]}
            />
          ),
        }}
        columns={[
          { title: '主机', dataIndex: 'hostName' },
          { title: 'Host ID', dataIndex: 'hostId', width: 200 },
          {
            title: '操作系统',
            render: (_, host) => `${host.osName ?? '-'} ${host.osVersion ?? ''}`,
          },
          {
            title: '遥测状态',
            dataIndex: 'telemetryStatus',
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
function Metric({ title, value }: { title: string; value?: number | null }) {
  return (
    <Card size="small">
      <Typography.Text type="secondary">{title}</Typography.Text>
      {value == null ? (
        <Typography.Text>-</Typography.Text>
      ) : (
        <Progress
          percent={Number(value.toFixed(1))}
          status={value >= 90 ? 'exception' : 'normal'}
        />
      )}
    </Card>
  );
}
function SnapshotSummary({ host, instance }: { host?: HostSnapshot; instance: InstanceSnapshot }) {
  const healthPresentation = monitorHealthPresentation(instance.health.status);
  return (
    <>
      <div className="sm-runtime-monitor-metrics">
        {host && <Metric title="主机 CPU" value={percent(host.cpu.usage)} />}
        <Metric title="进程 CPU" value={percent(instance.cpu.processUsage)} />
        {host && (
          <Metric
            title="物理内存"
            value={
              host.memory.collectorAvailable
                ? ratio(host.memory.total - host.memory.available, host.memory.total)
                : null
            }
          />
        )}
        <Metric
          title="JVM 堆"
          value={
            instance.memory.collectorAvailable
              ? ratio(instance.memory.heapUsed, instance.memory.heapMax)
              : null
          }
        />
        <Metric
          title="连接池"
          value={
            instance.dataSource.collectorAvailable
              ? ratio(instance.dataSource.active, instance.dataSource.maxActive)
              : null
          }
        />
      </div>
      <Card
        title={`${instance.instanceId} 实时快照`}
        extra={<Tag color={healthPresentation.color}>{healthPresentation.text}</Tag>}
      >
        <Descriptions
          size="small"
          column={4}
          items={[
            { key: 'host', label: 'Host ID', children: instance.hostId },
            { key: 'sample', label: '采样时间', children: instance.sampleTime },
            {
              key: 'os',
              label: '操作系统',
              children: host ? `${host.os.name} ${host.os.version}` : '当前主机遥测不可用',
            },
            { key: 'jvm', label: 'JVM', children: instance.runtime.vmName },
            {
              key: 'threads',
              label: '活动/阻塞线程',
              children: instance.threads.collectorAvailable
                ? `${instance.threads.live} / ${instance.threads.stateCounts.BLOCKED ?? 0}`
                : '-',
            },
            {
              key: 'http',
              label: 'HTTP 请求/5xx 速率',
              children: `${instance.http.requestRate?.toFixed(2) ?? '-'} / ${instance.http.serverErrorRate?.toFixed(2) ?? '-'} req/s`,
            },
            {
              key: 'latency',
              label: 'HTTP P95 / P99',
              children: `${instance.http.p95Ms?.toFixed(1) ?? '-'} / ${instance.http.p99Ms?.toFixed(1) ?? '-'} ms`,
            },
            {
              key: 'io',
              label: '磁盘读/写',
              children: host
                ? `${bytes(host.io.diskReadBytesPerSecond)}/s / ${bytes(host.io.diskWriteBytesPerSecond)}/s`
                : '-',
            },
          ]}
        />
      </Card>
    </>
  );
}
function FilesystemCard({ snapshot }: { snapshot: HostSnapshot }) {
  if (!snapshot.filesystemsAvailable) {
    return <Alert showIcon type="warning" title="文件系统指标暂不可用" />;
  }
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
            render: (_, item) => {
              const usage = ratio(item.used, item.total);
              return usage == null ? (
                '-'
              ) : (
                <Progress
                  percent={Number(usage.toFixed(1))}
                  status={usage >= 90 ? 'exception' : 'normal'}
                />
              );
            },
          },
        ]}
      />
    </Card>
  );
}
function historyOptions(
  scope: 'HOST' | 'INSTANCE',
  points: HistoryPoint[],
): Array<{ title: string; option: EChartsCoreOption }> {
  const groups: Array<{
    title: string;
    definitions: Array<[string, keyof HistoryPoint, number?]>;
  }> =
    scope === 'HOST'
      ? [
          {
            title: '主机利用率',
            definitions: [
              ['CPU %', 'cpuUsage', 100],
              ['内存 %', 'memoryUsage', 100],
              ['最高文件系统 %', 'filesystemUsage', 100],
            ],
          },
          {
            title: '磁盘 IO',
            definitions: [
              ['读 B/s', 'diskReadBytesPerSecond'],
              ['写 B/s', 'diskWriteBytesPerSecond'],
            ],
          },
          {
            title: '网络 IO',
            definitions: [
              ['接收 B/s', 'networkReceiveBytesPerSecond'],
              ['发送 B/s', 'networkTransmitBytesPerSecond'],
            ],
          },
        ]
      : [
          {
            title: 'JVM 利用率',
            definitions: [
              ['进程 CPU %', 'processCpuUsage', 100],
              ['Heap %', 'heapUsage', 100],
              ['连接池 %', 'dbPoolUsage', 100],
            ],
          },
          {
            title: 'HTTP 流量',
            definitions: [
              ['QPS', 'requestRate'],
              ['5xx req/s', 'serverErrorRate'],
            ],
          },
          {
            title: 'HTTP 延迟',
            definitions: [
              ['P95 ms', 'p95Ms'],
              ['P99 ms', 'p99Ms'],
            ],
          },
          {
            title: '线程与连接池等待',
            definitions: [
              ['线程数', 'threadCount'],
              ['阻塞线程', 'blockedThreadCount'],
              ['连接池等待', 'dbWaiting'],
            ],
          },
        ];
  return groups.map(({ title, definitions }) => ({
    title,
    option: {
      tooltip: { trigger: 'axis' },
      legend: { data: definitions.map((item) => item[0]) },
      grid: { left: 56, right: 24, top: 48, bottom: 32 },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: points.map((item) => item.sampleTime.replace('T', ' ').slice(0, 16)),
      },
      yAxis: { type: 'value' },
      series: definitions.map(([name, key, multiplier]) => ({
        name,
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: monitorHistoryValues(points, key, multiplier),
      })),
    },
  }));
}
