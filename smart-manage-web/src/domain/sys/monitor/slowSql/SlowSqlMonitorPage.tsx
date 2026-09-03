import { getBlockingQueryError } from '@/api/queryErrorFeedback';
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
import { useQuery, useQueryClient } from '@tanstack/react-query';
import type { PageComponentProps } from '@/domain/common/page/types';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { slowSqlApi } from './api';
import { slowSqlAccess } from './permissions';
import { slowSqlQueryKeys } from './queryKeys';
import type { SlowSqlSnapshot, SlowSqlStat } from './types';
import './slowSqlMonitor.css';

const PAGE_SIZE = 20;

export default function SlowSqlMonitorPage({ active }: PageComponentProps) {
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [selectedInstanceId, setSelectedInstanceId] = useState<string>();
  const [keyword, setKeyword] = useState('');
  const [thresholdInput, setThresholdInput] = useState<number>();
  const [pageNum, setPageNum] = useState(1);
  const { can } = usePermissionAccess(slowSqlAccess.prefix);
  const instancesQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: slowSqlQueryKeys.instances(),
    queryFn: slowSqlApi.instances,
    enabled: active,
    refetchInterval: active ? 10000 : false,
  });
  const effectiveInstanceId =
    selectedInstanceId ?? instancesQuery.data?.find((instance) => instance.current)?.instanceId;
  const snapshotQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: slowSqlQueryKeys.snapshot(effectiveInstanceId),
    queryFn: () => slowSqlApi.snapshot(effectiveInstanceId),
    enabled: active && Boolean(effectiveInstanceId),
  });
  const updateSnapshot = (snapshot: SlowSqlSnapshot) => {
    queryClient.setQueryData(slowSqlQueryKeys.snapshot(snapshot.instanceId), snapshot);
    setThresholdInput(undefined);
    setPageNum(1);
  };
  const thresholdMutation = useCommandMutation({
    mutationFn: () =>
      slowSqlApi.updateThreshold(
        effectiveInstanceId!,
        thresholdInput ?? snapshotQuery.data!.thresholdMs,
      ),
    onSuccess: updateSnapshot,
  });
  const clearMutation = useCommandMutation({
    mutationFn: () => slowSqlApi.clear(effectiveInstanceId!),
    onSuccess: updateSnapshot,
  });
  const filteredRecords = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return (snapshotQuery.data?.records ?? []).filter(
      (record) => !normalizedKeyword || record.sql.toLowerCase().includes(normalizedKeyword),
    );
  }, [keyword, snapshotQuery.data?.records]);
  const effectivePageNum = Math.min(
    pageNum,
    Math.max(1, Math.ceil(filteredRecords.length / PAGE_SIZE)),
  );
  const pagedRecords = filteredRecords.slice(
    (effectivePageNum - 1) * PAGE_SIZE,
    effectivePageNum * PAGE_SIZE,
  );

  return (
    <EditPageShell
      title="慢 SQL 分析"
      loading={instancesQuery.isLoading || snapshotQuery.isLoading}
      error={getBlockingQueryError(instancesQuery) ?? getBlockingQueryError(snapshotQuery)}
      onRetry={() => void Promise.all([instancesQuery.refetch(), snapshotQuery.refetch()])}
      actions={
        <Space size={10} wrap>
          <Select
            className="sm-slow-sql-instance"
            value={effectiveInstanceId}
            placeholder="选择在线实例"
            options={(instancesQuery.data ?? []).map((instance) => ({
              value: instance.instanceId,
              label: `${instance.instanceId}${instance.current ? '（当前）' : ''}`,
            }))}
            onChange={(instanceId) => {
              setSelectedInstanceId(instanceId);
              setThresholdInput(undefined);
              setPageNum(1);
            }}
          />
          <InputNumber
            value={thresholdInput ?? snapshotQuery.data?.thresholdMs}
            min={100}
            max={60000}
            disabled={!can('config')}
            onChange={(value) => setThresholdInput(value ?? undefined)}
          />
          <Typography.Text>ms</Typography.Text>
          {can('config') && (
            <Button
              type="primary"
              loading={thresholdMutation.isPending}
              disabled={!effectiveInstanceId}
              onClick={() => thresholdMutation.mutate()}
            >
              应用阈值
            </Button>
          )}
          <Button
            type="primary"
            loading={snapshotQuery.isFetching}
            onClick={() => void snapshotQuery.refetch()}
          >
            立即刷新
          </Button>
          {can('clear') && (
            <Button
              danger
              loading={clearMutation.isPending}
              disabled={!effectiveInstanceId}
              onClick={() =>
                void confirmOperation({
                  type: 'destructive',
                  title: '清空当前实例 SQL 统计？',
                  description: '内存统计清空后不可恢复，不影响数据库数据。',
                  confirmText: '确认清空',
                  onConfirm: () => clearMutation.mutateAsync(),
                })
              }
            >
              清空统计
            </Button>
          )}
        </Space>
      }
    >
      {snapshotQuery.data && (
        <div className="sm-slow-sql-monitor">
          <Alert
            type="info"
            showIcon
            title={`实例 ${snapshotQuery.data.instanceId} 的 Druid 内存聚合统计`}
            description={`采样时间 ${snapshotQuery.data.sampleTime}，当前阈值 ${snapshotQuery.data.thresholdMs} ms。数据在实例重启或清空后不可恢复；阈值变化不会追溯逐次执行记录。`}
          />
          <Card
            className="sm-slow-sql-card"
            title={
              <Space size={10} wrap>
                <Typography.Text strong>慢 SQL 列表</Typography.Text>
                <Tag>{filteredRecords.length} 条</Tag>
                <Input.Search
                  className="sm-slow-sql-search"
                  allowClear
                  placeholder="按 SQL 关键字筛选"
                  value={keyword}
                  onChange={(event) => {
                    setKeyword(event.target.value);
                    setPageNum(1);
                  }}
                />
              </Space>
            }
            extra={
              <Pagination
                size="small"
                current={effectivePageNum}
                pageSize={PAGE_SIZE}
                total={filteredRecords.length}
                showSizeChanger={false}
                onChange={setPageNum}
              />
            }
          >
            <Table<SlowSqlStat>
              className="sm-slow-sql-table"
              size="small"
              pagination={false}
              rowKey="id"
              dataSource={pagedRecords}
              sticky
              scroll={{ x: 1500, y: 1 }}
              expandable={{
                expandedRowRender: (record) => (
                  <Typography.Paragraph className="sm-slow-sql-full-text" copyable>
                    {record.sql}
                  </Typography.Paragraph>
                ),
              }}
              columns={[
                { title: 'SQL 模板', dataIndex: 'sql', ellipsis: true },
                {
                  title: '最大耗时（ms）',
                  dataIndex: 'executeMillisMax',
                  width: 150,
                  sorter: (left, right) => left.executeMillisMax - right.executeMillisMax,
                },
                {
                  title: '平均耗时（ms）',
                  dataIndex: 'executeMillisAverage',
                  width: 150,
                  render: (value: number) => value.toFixed(2),
                  sorter: (left, right) => left.executeMillisAverage - right.executeMillisAverage,
                },
                {
                  title: '执行次数',
                  dataIndex: 'executeCount',
                  width: 110,
                  sorter: (left, right) => left.executeCount - right.executeCount,
                },
                {
                  title: '错误次数',
                  dataIndex: 'errorCount',
                  width: 110,
                  sorter: (left, right) => left.errorCount - right.errorCount,
                },
                { title: '最大并发', dataIndex: 'concurrentMax', width: 110 },
                { title: '读取行数', dataIndex: 'fetchRowCount', width: 120 },
                { title: '更新行数', dataIndex: 'updateCount', width: 120 },
                {
                  title: '最近执行时间',
                  dataIndex: 'lastExecuteTime',
                  width: 150,
                },
              ]}
            />
          </Card>
        </div>
      )}
    </EditPageShell>
  );
}
