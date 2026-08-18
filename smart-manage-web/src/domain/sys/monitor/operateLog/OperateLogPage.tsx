import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import AuditLogFilter from '@/domain/sys/monitor/common/AuditLogFilter';
import { useAuditLogListQuery } from '@/domain/sys/monitor/common/useAuditLogListQuery';
import type { AuditLogFilters } from '@/domain/sys/monitor/common/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { operateLogApi } from './api';
import { operateLogQueryKeys } from './queryKeys';
import type { OperateLogListForm, OperateLogListVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

const OPERATE_LOG_DETAIL_KEY = 'sys/monitor/operate-log/detail';

const columnFeatures: ListColumnFeatures = {
  id: { label: '日志 ID', filter: { type: 'number' }, sorter: true },
  bizName: { label: '业务名称', filter: { type: 'string' } },
  username: { label: '操作人', filter: { type: 'string' } },
  requestMethod: {
    label: '请求方式',
    filter: {
      type: 'enum',
      options: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'].map((value) => ({ value, label: value })),
    },
  },
  requestUri: { label: '请求 URI', filter: { type: 'string' } },
  success: { label: '结果', filter: { type: 'boolean' } },
  durationMs: { label: '耗时', filter: { type: 'number' }, sorter: true },
  createTime: { label: '发生时间', filter: { type: 'date' }, sorter: true },
  traceId: { label: 'Trace ID', filter: { type: 'string' } },
};

const OperateLogPage = (props: PageComponentProps) => {
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const list = useAuditLogListQuery<OperateLogListVO, AuditLogFilters>({
    queryKey: operateLogQueryKeys.list,
    queryFn: (params) => operateLogApi.listPage(params as OperateLogListForm),
  });

  const columns: ColumnsType<OperateLogListVO> = [
    {
      title: '日志 ID',
      dataIndex: 'id',
      width: 210,
      fixed: 'left',
      render: (id: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(props.appNumber, OPERATE_LOG_DETAIL_KEY, record.id, OperationType.VIEW)
          }
        >
          {id}
        </Button>
      ),
    },
    { title: '业务名称', dataIndex: 'bizName' },
    { title: '操作人', dataIndex: 'username', width: 140 },
    { title: '请求方式', dataIndex: 'requestMethod', width: 90 },
    { title: '请求 URI', dataIndex: 'requestUri', width: 260 },
    {
      title: '结果',
      dataIndex: 'success',
      width: 80,
      render: (value: boolean) =>
        value ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
    },
    {
      title: '耗时',
      dataIndex: 'durationMs',
      width: 100,
      render: (value?: number) => (value === undefined ? '-' : `${value} ms`),
    },
    { title: '发生时间', dataIndex: 'createTime', width: 180 },
    { title: 'Trace ID', dataIndex: 'traceId', width: 260 },
  ];

  return (
    <ListPage<OperateLogListVO>
      {...props}
      title="操作日志"
      loading={list.query.isLoading}
      error={list.query.error as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索业务、用户、URI 或方法"
      filterSummary={list.keyword ? `关键字：${list.keyword}` : undefined}
      filterContent={<AuditLogFilter values={list.filters} onFilter={list.onFilter} />}
      onQuickSearch={list.onQuickSearch}
      onPageChange={list.onPageChange}
      onRefresh={list.onRefresh}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...list.columnQueryProps}
      dataSource={list.records}
    />
  );
};

export default OperateLogPage;
