import { useState } from 'react';
import { Button, Select, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { executionApi } from './api';
import { executionQueryKeys } from './queryKeys';
import type { ExecutionStatus, ExecutionVO } from './types';

const DETAIL_KEY = 'sys/scheduler/execution/detail';

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
          onClick={() =>
            openBillTab(props.appNumber, DETAIL_KEY, '执行实例详情', id, OperationType.VIEW)
          }
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
          options={[
            { label: '运行中', value: 'RUNNING' },
            { label: '成功', value: 'SUCCESS' },
            { label: '失败', value: 'FAILED' },
            { label: '互斥跳过', value: 'SKIPPED' },
          ]}
          style={{ width: 140 }}
          onChange={(value) => setStatus(value)}
        />
      }
      onRefresh={list.onRefresh}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      dataSource={list.records}
    />
  );
};

export default ExecutionListPage;
