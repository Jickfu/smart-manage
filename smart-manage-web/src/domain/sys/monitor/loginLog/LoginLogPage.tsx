import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
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
  LOGIN: '登录',
  LOGOUT: '退出',
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
            openBillTab(
              props.appNumber,
              LOGIN_LOG_DETAIL_KEY,
              '登录日志详情',
              record.id,
              OperationType.VIEW,
            )
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
      error={list.query.error as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索用户名、昵称或 IP"
      filterSummary={list.keyword ? `关键字：${list.keyword}` : undefined}
      filterContent={
        <AuditLogFilter
          values={list.filters}
          eventTypeOptions={[
            { label: '登录', value: 'LOGIN' },
            { label: '退出', value: 'LOGOUT' },
          ]}
          onFilter={list.onFilter}
        />
      }
      onQuickSearch={list.onQuickSearch}
      onPageChange={list.onPageChange}
      onRefresh={list.onRefresh}
      rowKey="id"
      columns={columns}
      dataSource={list.records}
    />
  );
};

export default LoginLogPage;
