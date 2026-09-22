import { ListFilterField, ListFilterFields } from '@/domain/common/page/list/ListFilterFields';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { SchedulerScopeTree } from '../common/SchedulerScopeTree';
import { parseSchedulerScope } from '../common/schedulerScope';
import { Button, Select, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { executionApi } from './api';
import { executionQueryKeys } from './queryKeys';
import type { ExecutionStatus, ExecutionVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

const DETAIL_KEY = componentKeys.schedulerExecutionDetail;

const executionStatusOptions = [
  { label: '运行中', value: 'RUNNING' },
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILED' },
  { label: '互斥跳过', value: 'SKIPPED' },
];

const columnFeatures: ListColumnFeatures = {
  id: { label: '实例 ID', filter: { type: 'number' } },
  jobName: { label: '任务名称', filter: { type: 'string' } },
  appName: { label: '所属应用', filter: { type: 'string' } },
  status: {
    label: '状态',
    filter: { type: 'enum', options: executionStatusOptions },
    sorter: true,
  },
  startTime: { label: '开始时间', filter: { type: 'date' }, sorter: true },
  endTime: { label: '结束时间', filter: { type: 'date' }, sorter: true },
  durationMs: { label: '耗时', filter: { type: 'number' }, sorter: true },
  traceId: { label: 'Trace ID', filter: { type: 'string' } },
  errorMessage: { label: '错误信息', filter: { type: 'string' } },
};

const ExecutionListPage = (props: PageComponentProps) => {
  const [status, setStatus] = useState<ExecutionStatus>();
  const [scopeKey, setScopeKey] = useState('all');
  const scope = parseSchedulerScope(scopeKey);
  const catalogQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: executionQueryKeys.catalog(),
    queryFn: executionApi.catalog,
  });
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const list = useListPageQuery({
    queryKey: executionQueryKeys.list(status, scope),
    queryFn: (params) => executionApi.listPage({ ...params, status, ...scope }),
  });
  const columns: ColumnsType<ExecutionVO> = [
    {
      title: '实例 ID',
      dataIndex: 'id',
      width: 210,
      fixed: 'left',
      render: (id: string) => (
        <Button
          type="link"
          size="small"
          onClick={() => openBillTab(props.appNumber, DETAIL_KEY, id, OperationType.VIEW)}
        >
          {id}
        </Button>
      ),
    },
    { title: '任务名称', dataIndex: 'jobName', width: 180 },
    { title: '所属应用', dataIndex: 'appName', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value: ExecutionStatus) => {
        if (value === 'SUCCESS') return <Tag color="success">成功</Tag>;
        if (value === 'FAILED') return <Tag color="error">失败</Tag>;
        if (value === 'SKIPPED') return <Tag>互斥跳过</Tag>;
        return <Tag color="processing">运行中</Tag>;
      },
    },
    { title: '开始时间', dataIndex: 'startTime', width: 180 },
    { title: '结束时间', dataIndex: 'endTime', width: 180 },
    {
      title: '耗时',
      dataIndex: 'durationMs',
      width: 110,
      render: (value?: number | null) => (value == null ? '-' : `${value} ms`),
    },
    { title: 'Trace ID', dataIndex: 'traceId', width: 300 },
    { title: '错误信息', dataIndex: 'errorMessage', ellipsis: true },
  ];

  return (
    <ListPage<ExecutionVO>
      {...props}
      title="执行记录"
      loading={list.query.isLoading || catalogQuery.isLoading}
      error={
        (getBlockingQueryError(list.query) || getBlockingQueryError(catalogQuery)) as Error | null
      }
      onRetry={() => Promise.all([list.query.refetch(), catalogQuery.refetch()])}
      treePanel={
        <SchedulerScopeTree
          title="全部执行记录"
          nodes={catalogQuery.data}
          selectedKey={scopeKey}
          onSelect={(nextKey) => {
            setScopeKey(nextKey);
            list.resetPage();
          }}
        />
      }
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索任务名称"
      filterSummary={
        status
          ? `状态：${executionStatusOptions.find((option) => option.value === status)?.label ?? status}`
          : undefined
      }
      filterContent={
        <ListFilterFields>
          <ListFilterField label="状态">
            <Select
              allowClear
              placeholder="全部状态"
              value={status}
              options={executionStatusOptions}
              onChange={(value) => {
                setStatus(value);
                list.resetPage();
              }}
            />
          </ListFilterField>
        </ListFilterFields>
      }
      onRefresh={() => {
        void Promise.all([list.query.refetch(), catalogQuery.refetch()]);
      }}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...list.columnQueryProps}
      dataSource={list.records}
    />
  );
};

export default ExecutionListPage;
