import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/list/ListPage';
import ListFilterSummary from '@/domain/common/page/list/ListFilterSummary';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import AuditLogFilter from '@/domain/sys/monitor/common/AuditLogFilter';
import { useAuditLogListQuery } from '@/domain/sys/monitor/common/useAuditLogListQuery';
import type { AuditLogFilters } from '@/domain/sys/monitor/common/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { loginLogApi } from './api';
import { loginLogQueryKeys } from './queryKeys';
import type { LoginEventType, LoginLogListForm, LoginLogListVO } from './types';

interface LoginLogFilters extends AuditLogFilters {
  eventType?: LoginEventType;
}

const LOGIN_LOG_DETAIL_KEY = 'sys/monitor/login-log/detail';

const eventTypeLabels: Record<LoginEventType, string> = {
  LOGIN_SUCCESS: '登录成功',
  LOGIN_FAILURE: '登录失败',
  PASSWORD_CHANGE_REQUIRED: '要求修改密码',
  LOGOUT: '退出',
  SESSION_KICKED: '会话被踢下线',
  SESSION_REPLACED: '会话被顶替',
  ACCOUNT_DISABLED: '账号禁用',
  PASSWORD_RESET_TERMINATED: '重置密码下线',
  TEMPORARY_LOGIN_GRANT_CREATED: '生成代登录密码',
  TEMPORARY_LOGIN_SUCCESS: '代登录成功',
};

const eventTypeOptions = Object.entries(eventTypeLabels).map(([value, label]) => ({
  value,
  label,
}));

const columnFeatures: ListColumnFeatures = {
  id: { label: '日志 ID', filter: { type: 'number' }, sorter: true },
  username: { label: '用户名', filter: { type: 'string' } },
  nickname: { label: '昵称', filter: { type: 'string' } },
  eventType: { label: '事件', filter: { type: 'enum', options: eventTypeOptions } },
  success: {
    label: '结果',
    filter: {
      type: 'boolean',
      options: [
        { label: '成功', value: true },
        { label: '失败', value: false },
      ],
    },
  },
  ip: { label: 'IP 地址', filter: { type: 'string' } },
  createTime: { label: '发生时间', filter: { type: 'date' }, sorter: true },
  traceId: { label: 'Trace ID', filter: { type: 'string' } },
};

const LoginLogPage = (props: PageComponentProps) => {
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const list = useAuditLogListQuery<LoginLogListVO, LoginLogFilters>({
    queryKey: loginLogQueryKeys.list,
    queryFn: (params) => loginLogApi.listPage(params as LoginLogListForm),
  });

  const columns: ColumnsType<LoginLogListVO> = [
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
            openBillTab(props.appNumber, LOGIN_LOG_DETAIL_KEY, record.id, OperationType.VIEW)
          }
        >
          {id}
        </Button>
      ),
    },
    { title: '用户名', dataIndex: 'username', width: 150 },
    { title: '昵称', dataIndex: 'nickname' },
    {
      title: '事件',
      dataIndex: 'eventType',
      width: 90,
      render: (value: LoginEventType) => eventTypeLabels[value] ?? value,
    },
    {
      title: '结果',
      dataIndex: 'success',
      width: 80,
      render: (value: boolean) =>
        value ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
    },
    { title: 'IP 地址', dataIndex: 'ip', width: 150 },
    { title: '发生时间', dataIndex: 'createTime', width: 180 },
    { title: 'Trace ID', dataIndex: 'traceId', width: 260 },
  ];

  return (
    <ListPage<LoginLogListVO>
      {...props}
      title="登录日志"
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索用户名、昵称或 IP"
      filterSummary={
        <ListFilterSummary
          items={[
            ...(list.filters.success === undefined
              ? []
              : [
                  {
                    key: 'success',
                    label: `结果：${list.filters.success ? '成功' : '失败'}`,
                    onRemove: () => list.onFilter({ ...list.filters, success: undefined }),
                  },
                ]),
            ...(list.filters.eventType
              ? [
                  {
                    key: 'eventType',
                    label: `事件：${eventTypeLabels[list.filters.eventType]}`,
                    onRemove: () => list.onFilter({ ...list.filters, eventType: undefined }),
                  },
                ]
              : []),
            ...(list.filters.timeRange
              ? [
                  {
                    key: 'timeRange',
                    label: '发生时间：已设置',
                    onRemove: () => list.onFilter({ ...list.filters, timeRange: undefined }),
                  },
                ]
              : []),
            ...(list.filters.traceId
              ? [
                  {
                    key: 'traceId',
                    label: `Trace ID：${list.filters.traceId}`,
                    onRemove: () => list.onFilter({ ...list.filters, traceId: undefined }),
                  },
                ]
              : []),
          ]}
        />
      }
      filterContent={
        <AuditLogFilter
          values={list.filters}
          eventTypeOptions={eventTypeOptions}
          onFilter={list.onFilter}
        />
      }
      onQuickSearch={list.onQuickSearch}
      onPageChange={list.onPageChange}
      onRefresh={list.onRefresh}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      columnFilters={list.columnFilters}
      columnSort={list.columnSort}
      onColumnFiltersChange={list.onColumnFiltersChange}
      onColumnSortChange={list.onColumnSortChange}
      dataSource={list.records}
    />
  );
};

export default LoginLogPage;
