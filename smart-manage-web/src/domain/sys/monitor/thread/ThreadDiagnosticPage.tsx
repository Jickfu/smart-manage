import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Input,
  InputNumber,
  Pagination,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/edit/EditPageShell';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { threadDiagnosticApi } from './api';
import { formatCpuUsage } from './formatters';
import { threadDiagnosticAccess } from './permissions';
import { threadDiagnosticQueryKeys } from './queryKeys';
import type { ThreadDiagnosticResult, ThreadItem } from './types';
import './threadDiagnostic.css';

const stateColors: Record<string, string> = {
  RUNNABLE: 'processing',
  BLOCKED: 'error',
  WAITING: 'warning',
  TIMED_WAITING: 'default',
};

export default function ThreadDiagnosticPage({ active }: PageComponentProps) {
  const feedback = useOperationFeedback();
  const [selectedInstanceId, setSelectedInstanceId] = useState<string>();
  const [selectedThreadId, setSelectedThreadId] = useState<number>();
  const [nameFilter, setNameFilter] = useState('');
  const [stateFilter, setStateFilter] = useState<string>();
  const [sampleMillis, setSampleMillis] = useState(1000);
  const [limit, setLimit] = useState(10);
  const [pageNum, setPageNum] = useState(1);
  const [collectedResult, setCollectedResult] = useState<ThreadDiagnosticResult>();
  const { can } = usePermissionAccess(threadDiagnosticAccess.prefix);
  const instancesQuery = useQuery({
    queryKey: threadDiagnosticQueryKeys.instances(),
    queryFn: threadDiagnosticApi.instances,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const effectiveInstanceId =
    selectedInstanceId ?? instancesQuery.data?.find((instance) => instance.current)?.instanceId;
  const listQuery = useQuery({
    queryKey: threadDiagnosticQueryKeys.list(effectiveInstanceId),
    queryFn: () => threadDiagnosticApi.list(effectiveInstanceId),
    enabled: active && Boolean(effectiveInstanceId),
  });
  const detailQuery = useQuery({
    queryKey: threadDiagnosticQueryKeys.detail(effectiveInstanceId, selectedThreadId),
    queryFn: () => threadDiagnosticApi.detail(effectiveInstanceId, selectedThreadId!),
    enabled: active && Boolean(effectiveInstanceId) && selectedThreadId !== undefined,
  });
  const collectMutation = useCommandMutation({
    mutationFn: ({ action }: { action: 'hot' | 'dump' | 'deadlocks' }) =>
      threadDiagnosticApi[action]({
        instanceId: effectiveInstanceId,
        sampleMillis,
        limit,
        maxDepth: action === 'deadlocks' ? 128 : 64,
      }),
    onSuccess: (result) => {
      setCollectedResult(result);
      setSelectedThreadId(result.threads[0]?.id);
    },
  });

  const displayedResult = collectedResult ?? listQuery.data;
  const filteredThreads = useMemo(() => {
    const normalizedName = nameFilter.trim().toLowerCase();
    return (displayedResult?.threads ?? []).filter(
      (thread) =>
        (!normalizedName || thread.name.toLowerCase().includes(normalizedName)) &&
        (!stateFilter || thread.state === stateFilter),
    );
  }, [displayedResult, nameFilter, stateFilter]);
  const selectedThread =
    detailQuery.data?.threads[0] ??
    displayedResult?.threads.find((thread) => thread.id === selectedThreadId);
  const selectedStackText = selectedThread
    ? selectedThread.stackTrace.length
      ? selectedThread.stackTrace.map((frame) => `at ${frame}`).join('\n')
      : '当前快照没有可用堆栈，请选择线程或重新采集。'
    : '';
  const copyStack = async () => {
    try {
      await navigator.clipboard.writeText(selectedStackText);
      feedback.success('线程栈已复制');
    } catch {
      feedback.error('复制失败，请检查浏览器剪贴板权限');
    }
  };
  const pageSize = 20;
  const effectivePageNum = Math.min(
    pageNum,
    Math.max(1, Math.ceil(filteredThreads.length / pageSize)),
  );
  const pagedThreads = filteredThreads.slice(
    (effectivePageNum - 1) * pageSize,
    effectivePageNum * pageSize,
  );

  return (
    <EditPageShell
      title="线程诊断"
      loading={instancesQuery.isLoading || listQuery.isLoading}
      error={instancesQuery.error ?? listQuery.error}
      onRetry={() => void Promise.all([instancesQuery.refetch(), listQuery.refetch()])}
      actions={
        <div className="sm-thread-diagnostic-toolbar">
          <Select
            className="sm-thread-diagnostic-instance"
            value={effectiveInstanceId}
            placeholder="选择在线实例"
            options={(instancesQuery.data ?? []).map((instance) => ({
              value: instance.instanceId,
              label: `${instance.instanceId}${instance.current ? '（当前）' : ''}`,
            }))}
            onChange={(instanceId) => {
              setSelectedInstanceId(instanceId);
              setSelectedThreadId(undefined);
              setCollectedResult(undefined);
              setPageNum(1);
            }}
          />
          <Input
            className="sm-thread-diagnostic-name-filter"
            allowClear
            placeholder="过滤线程名称"
            value={nameFilter}
            onChange={(event) => {
              setNameFilter(event.target.value);
              setPageNum(1);
            }}
          />
          <Select
            className="sm-thread-diagnostic-state-filter"
            allowClear
            placeholder="线程状态"
            value={stateFilter}
            options={['RUNNABLE', 'BLOCKED', 'WAITING', 'TIMED_WAITING', 'NEW', 'TERMINATED'].map(
              (state) => ({ value: state, label: state }),
            )}
            onChange={(value) => {
              setStateFilter(value);
              setPageNum(1);
            }}
          />
          <Space size={10}>
            <Typography.Text>采样时间</Typography.Text>
            <InputNumber
              min={200}
              max={5000}
              value={sampleMillis}
              onChange={(value) => setSampleMillis(value ?? 1000)}
            />
            <Typography.Text>ms</Typography.Text>
          </Space>
          <Space size={10}>
            <Typography.Text>热点数量</Typography.Text>
            <InputNumber
              min={1}
              max={50}
              value={limit}
              onChange={(value) => setLimit(value ?? 10)}
            />
          </Space>
          <Button type="primary" onClick={() => void listQuery.refetch()}>
            刷新列表
          </Button>
          {can(threadDiagnosticAccess.permissions.collect) && (
            <Space size={10}>
              <Button
                type="primary"
                loading={collectMutation.isPending}
                onClick={() => collectMutation.mutate({ action: 'hot' })}
              >
                采集热点
              </Button>
              <Button
                type="primary"
                loading={collectMutation.isPending}
                onClick={() => collectMutation.mutate({ action: 'deadlocks' })}
              >
                检测死锁
              </Button>
              <Button
                danger
                loading={collectMutation.isPending}
                onClick={() => collectMutation.mutate({ action: 'dump' })}
              >
                全量快照
              </Button>
            </Space>
          )}
        </div>
      }
    >
      <div className="sm-thread-diagnostic">
        <Alert
          type="info"
          showIcon
          title="JDK 原生线程诊断"
          description="展示指定在线实例的平台线程、实时堆栈、CPU 热点和锁信息；采集结果是瞬时快照，不代表持久化监控历史。"
        />
        <div className="sm-thread-diagnostic-grid">
          <Card
            className="sm-thread-diagnostic-card"
            title={`线程列表 · ${displayedResult?.instanceId ?? '未选择实例'} · 共 ${filteredThreads.length} 条`}
            extra={
              <Space size={10}>
                <Typography.Text type="secondary">{displayedResult?.sampleTime}</Typography.Text>
                <Pagination
                  size="small"
                  current={effectivePageNum}
                  pageSize={pageSize}
                  total={filteredThreads.length}
                  showSizeChanger={false}
                  onChange={setPageNum}
                />
              </Space>
            }
          >
            <Table<ThreadItem>
              size="small"
              rowKey="id"
              pagination={false}
              dataSource={pagedThreads}
              onRow={(thread) => ({ onClick: () => setSelectedThreadId(thread.id) })}
              columns={[
                { title: 'ID', dataIndex: 'id', width: 90 },
                { title: '线程名称', dataIndex: 'name', ellipsis: true },
                {
                  title: '状态',
                  dataIndex: 'state',
                  width: 130,
                  render: (state: string, thread) => (
                    <Tag color={thread.deadlocked ? 'error' : stateColors[state]}>{state}</Tag>
                  ),
                },
                {
                  title: 'CPU',
                  dataIndex: 'cpuUsage',
                  width: 90,
                  render: formatCpuUsage,
                },
              ]}
            />
          </Card>

          <Card
            className="sm-thread-diagnostic-card"
            title="线程栈详情"
            extra={
              <Button disabled={!selectedThread} onClick={() => void copyStack()}>
                复制
              </Button>
            }
          >
            {selectedThread ? (
              <Space className="sm-thread-stack-content" orientation="vertical" size={10}>
                <Space wrap size={10}>
                  <Tag>{selectedThread.id}</Tag>
                  <Tag color={stateColors[selectedThread.state]}>{selectedThread.state}</Tag>
                  <Typography.Text>{selectedThread.name}</Typography.Text>
                </Space>
                {(selectedThread.lockName || selectedThread.lockOwnerName) && (
                  <Alert
                    type="warning"
                    showIcon
                    title={`等待锁：${selectedThread.lockName ?? '-'}；持有线程：${selectedThread.lockOwnerName ?? '-'}`}
                  />
                )}
                <pre className="sm-thread-stack">{selectedStackText}</pre>
              </Space>
            ) : (
              <div className="sm-thread-stack-empty">选择线程后查看当前调用栈。</div>
            )}
          </Card>
        </div>
      </div>
    </EditPageShell>
  );
}
