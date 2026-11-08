import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import {
  RequestErrorDescription,
  RequestErrorState,
} from '@/domain/common/component/RequestErrorState';
import AppModal from '@/domain/common/component/AppModal';
import ListTableShell from '@/domain/common/page/list/ListTableShell';
import { isErrorFeedbackSuppressed } from '@/api/errorPresentation';
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
import { createHistoryCharts } from './historyCharts';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { runtimeMonitorApi } from './api';
import { runtimeMonitorQueryKeys as keys } from './queryKeys';
import type { HistoryPoint, HostSnapshot, InstanceSnapshot, MonitorHost } from './types';
import {
  monitorBytes as bytes,
  monitorHealthPresentation,
  monitorPercent as percent,
  monitorRatio as ratio,
} from './formatters';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
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
  const [dataChartTitle, setDataChartTitle] = useState<string>();
  const [selectedLifecycleInstanceId, setSelectedLifecycleInstanceId] = useState<string>();
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  const { can } = usePermissionAccess(runtimeMonitorAccess.prefix);
  const topology = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: keys.topology(),
    queryFn: runtimeMonitorApi.topology,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const instances = useQuery({
    meta: { errorPresentation: 'local-initial' },
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
    meta: { errorPresentation: 'local' },
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
  const charts = useMemo(
    () => createHistoryCharts(scope, history.data ?? [], range),
    [history.data, scope, range],
  );
  const dataChart = charts.find((chart) => chart.title === dataChartTitle);
  const pageError = getBlockingQueryError(topology) ?? getBlockingQueryError(instances);
  const historyError = getBlockingQueryError(history);
  return (
    <EditPageShell
      title="运行监控"
      loading={topology.isLoading || instances.isLoading}
      error={pageError}
      onRetry={() => {
        void (topology.isEnabled && topology.refetch());
        void (instances.isEnabled && instances.refetch());
        void (instanceSnapshot.isEnabled && instanceSnapshot.refetch());
        void (hostSnapshot.isEnabled && hostSnapshot.refetch());
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
            onChange={(value) => {
              setDataChartTitle(undefined);
              setInstanceId(value);
            }}
          />
          <Segmented
            value={scope}
            options={[
              { label: '实例趋势', value: 'INSTANCE' },
              { label: '主机趋势', value: 'HOST' },
            ]}
            onChange={(value) => {
              setDataChartTitle(undefined);
              setScope(value as 'HOST' | 'INSTANCE');
            }}
          />
          <Select
            value={range}
            options={ranges}
            onChange={(value) => {
              setDataChartTitle(undefined);
              setRange(value);
            }}
          />
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
        {selectedInstance &&
          (selectedInstance.lifecycle === 'RETIRED' || !selectedInstance.online) && (
            <Alert
              showIcon
              type={selectedInstance.lifecycle === 'RETIRED' ? 'info' : 'warning'}
              title={
                selectedInstance.lifecycle === 'RETIRED'
                  ? '当前实例已退役，仍可查看历史趋势'
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
        {history.error && !isErrorFeedbackSuppressed(history.error) && (
          <Alert
            showIcon
            type="error"
            title="历史趋势加载失败"
            description={
              <RequestErrorDescription error={history.error} fallbackMessage="历史数据加载失败" />
            }
          />
        )}
        {!historyError &&
          charts.map((chart) => (
            <Card
              key={chart.title}
              title={chart.title}
              loading={history.isLoading}
              extra={
                <Button
                  type="link"
                  disabled={history.data === undefined}
                  onClick={() => setDataChartTitle(chart.title)}
                >
                  查看数据
                </Button>
              }
            >
              <Suspense fallback={<div>正在加载图表</div>}>
                <SmChart option={chart.option} ariaLabel={chart.title} />
              </Suspense>
            </Card>
          ))}
        {hostSnapshot.data?.snapshot && <FilesystemCard snapshot={hostSnapshot.data.snapshot} />}
        <AppModal
          title={`${dataChartTitle ?? '历史趋势'}数据`}
          open={Boolean(dataChart) && !pageError}
          width={1000}
          bodyMode="fixed"
          classNames={{ body: 'sm-runtime-history-data-body' }}
          onCancel={() => setDataChartTitle(undefined)}
          footer={<Button onClick={() => setDataChartTitle(undefined)}>关闭</Button>}
        >
          <p>
            {scope === 'HOST' ? '主机' : '实例'}：{scopeId}；查询范围：
            {ranges.find((item) => item.value === range)?.label}。
          </p>
          <p>
            显示当前范围全部聚合统计点，图例显隐不改变本表口径。“—”表示缺失，0
            表示实际零值；比例保留接口原值，图表乘以 100 显示百分比，其余单位见列名。
          </p>
          <p>
            时间为统计桶起点；1 小时／6 小时／24 小时／7 天分别按 1／5／10／30 分钟聚合。
            最高文件系统使用率及 P95/P99 取桶内最大值，其余指标取有效值平均；P95/P99
            不代表整个时间段重新计算的百分位。
          </p>
          {historyError ? (
            <RequestErrorState error={historyError} onRetry={() => void history.refetch()} />
          ) : (
            <>
              {history.error && !isErrorFeedbackSuppressed(history.error) && (
                <p>刷新失败，当前展示上次成功加载的数据。</p>
              )}
              <div className="sm-runtime-history-data-table">
                <ListTableShell
                  total={history.data?.length ?? 0}
                  showPagination={false}
                  table={
                    <Table<HistoryPoint>
                      className="sm-list-table sm-table-fill"
                      size="small"
                      rowKey="sampleTime"
                      pagination={false}
                      columns={dataChart?.columns}
                      dataSource={history.data ?? []}
                      // 仅启用表头分离，实际高度及底部横向滚动由既有列表 flex 布局控制。
                      scroll={{ x: 'max-content', y: 1 }}
                    />
                  }
                />
              </div>
            </>
          )}
        </AppModal>
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
                { ellipsis: true, title: '实例 ID', dataIndex: 'instanceId' },
                { ellipsis: true, title: '应用', dataIndex: 'applicationName', width: 180 },
                { ellipsis: true, title: '版本', dataIndex: 'applicationVersion', width: 140 },
                { ellipsis: true, title: '生命周期', dataIndex: 'lifecycle', width: 110 },
                {
                  ellipsis: true,
                  title: '状态',
                  dataIndex: 'online',
                  width: 120,
                  render: (online: boolean) => (
                    <Tag color={online ? 'success' : 'default'}>{online ? '在线' : '离线'}</Tag>
                  ),
                },
                { ellipsis: true, title: '最后发现', dataIndex: 'lastSeenTime', width: 180 },
              ]}
            />
          ),
        }}
        columns={[
          { ellipsis: true, title: '主机', dataIndex: 'hostName' },
          { ellipsis: true, title: 'Host ID', dataIndex: 'hostId', width: 200 },
          {
            ellipsis: true,
            title: '操作系统',
            render: (_, host) => `${host.osName ?? '-'} ${host.osVersion ?? ''}`,
          },
          {
            ellipsis: true,
            title: '遥测状态',
            dataIndex: 'telemetryStatus',
            width: 180,
            render: (status: string) => (
              <Tag color={status === 'UP' ? 'success' : 'warning'}>
                {status === 'UP' ? '可用' : '主机遥测不可用'}
              </Tag>
            ),
          },
          {
            ellipsis: true,
            title: '实例数',
            width: 100,
            render: (_, host) => host.instances.length,
          },
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
          { ellipsis: true, title: '挂载点', dataIndex: 'mount' },
          { ellipsis: true, title: '名称', dataIndex: 'name' },
          { ellipsis: true, title: '类型', dataIndex: 'type', width: 120 },
          { ellipsis: true, title: '已用', width: 140, render: (_, item) => bytes(item.used) },
          { ellipsis: true, title: '可用', width: 140, render: (_, item) => bytes(item.available) },
          {
            ellipsis: true,
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
