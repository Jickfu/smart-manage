import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import { editFormSection } from '@/domain/common/page/editPageSection';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { loginLogApi } from './api';
import { loginLogQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '日志 ID', dataIndex: 'id', type: 'text' },
  { label: '用户名', dataIndex: 'username', type: 'text' },
  { label: '昵称', dataIndex: 'nickname', type: 'text' },
  { label: '结果', dataIndex: 'successLabel', type: 'text' },
  {
    label: '事件',
    dataIndex: 'eventType',
    type: 'text',
  },
  { label: 'IP 地址', dataIndex: 'ip', type: 'text' },
  { label: '发生时间', dataIndex: 'createTime', type: 'datetime' },
  { label: 'Trace ID', dataIndex: 'traceId', type: 'text' },
  { label: '签发管理员 ID', dataIndex: 'issuerUserId', type: 'text' },
  { label: '代登录授权编号', dataIndex: 'grantId', type: 'text' },
  { label: '凭证失效时间', dataIndex: 'grantExpiresAt', type: 'datetime' },
  { label: '代登录原因', dataIndex: 'grantReason', type: 'textarea', fullWidth: true },
  { label: '失败原因', dataIndex: 'failReason', type: 'textarea', fullWidth: true },
  { label: 'User-Agent', dataIndex: 'userAgent', type: 'textarea', fullWidth: true },
];

const LoginLogDetailPage = (props: PageComponentProps) => {
  const detailQuery = useQuery({
    queryKey: loginLogQueryKeys.detail(props.billId),
    queryFn: () => loginLogApi.detail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const initialValues = useMemo(() => {
    const detail = detailQuery.data;
    if (!detail) return {};
    return {
      ...detail,
      successLabel: detail.success ? '成功' : '失败',
    };
  }, [detailQuery.data]);

  return (
    <EditPage
      title="登录日志"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={OperationType.VIEW}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
};

export default LoginLogDetailPage;
