import { useState } from 'react';
import { Button, Select, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import './ExecutionListPage.css';
import { useWorkbenchStore } from '@/stores/workbench';
import { executionApi } from './api';
import { executionQueryKeys } from './queryKeys';
import type { ExecutionStatus, ExecutionVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

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
  jobGroup: { label: '任务分组', filter: { type: 'string' } },
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
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const list = useListPageQuery({
    queryKey: executionQueryKeys.list(status),
    queryFn: (params) => executionApi.listPage({ ...params, status }),
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
    { title: '任务分组', dataIndex: 'jobGroup', width: 140 },
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
      render: (value?: number) => (value === undefined ? '-' : `${value} ms`),
    },
    { title: 'Trace ID', dataIndex: 'traceId', width: 260 },
    { title: '错误信息', dataIndex: 'errorMessage', ellipsis: true },
  ];

  return (
    <ListPage<ExecutionVO>
      {...props}
      title="执行实例"
      loading={list.query.isLoading}
      error={list.query.error as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索任务名称"
      filterContent={
        <Select
          allowClear
          placeholder="全部状态"
          value={status}
          options={executionStatusOptions}
          className="sm-execution-status-filter"
          onChange={(value) => setStatus(value)}
        />
      }
      onRefresh={list.onRefresh}
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
