import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { EditField } from '@/domain/common/page/EditPage';
import EditPage from '@/domain/common/page/EditPage';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { operateLogApi } from './api';
import { operateLogQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '日志 ID', dataIndex: 'id', type: 'text' },
  { label: '业务名称', dataIndex: 'bizName', type: 'text' },
  { label: '操作人', dataIndex: 'username', type: 'text' },
  { label: '结果', dataIndex: 'successLabel', type: 'text' },
  {
    label: '请求方式',
    dataIndex: 'requestMethod',
    type: 'select',
    options: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE'].map((method) => ({
      label: method,
      value: method,
    })),
  },
  { label: 'IP 地址', dataIndex: 'ip', type: 'text' },
  { label: '耗时（毫秒）', dataIndex: 'durationMs', type: 'number' },
  { label: '发生时间', dataIndex: 'createTime', type: 'datetime' },
  { label: '请求 URI', dataIndex: 'requestUri', type: 'text', columnSpan: 2 },
  { label: '执行类', dataIndex: 'className', type: 'text', columnSpan: 2 },
  { label: 'Trace ID', dataIndex: 'traceId', type: 'text' },
  { label: '执行方法', dataIndex: 'methodName', type: 'text' },
  { label: 'User-Agent', dataIndex: 'userAgent', type: 'textarea', fullWidth: true },
  { label: '错误信息', dataIndex: 'errorMsg', type: 'textarea', fullWidth: true },
  { label: '请求参数', dataIndex: 'requestParams', type: 'textarea', fullWidth: true },
  { label: '响应内容', dataIndex: 'responseBody', type: 'textarea', fullWidth: true },
];

const OperateLogDetailPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const detailQuery = useQuery({
    queryKey: operateLogQueryKeys.detail(props.billId),
    queryFn: () => operateLogApi.detail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () =>
      detail
        ? {
            ...detail,
            successLabel: detail.success ? '成功' : '失败',
          }
        : {},
    [detail],
  );

  const handleCopy = async () => {
    if (!detail) return;
    try {
      // detail 即接口响应的 data 部分，复制时保留完整字段并格式化为可读 JSON。
      await navigator.clipboard.writeText(JSON.stringify(detail, null, 2));
      feedback.success('日志数据已复制');
    } catch {
      feedback.error('复制失败，请检查浏览器剪贴板权限');
    }
  };

  return (
    <EditPage
      title="操作日志"
      fields={fields}
      initialValues={initialValues}
      operationType={OperationType.VIEW}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      headerActions={[
        {
          key: 'copy',
          label: '复制',
          disabled: !detail,
          onClick: handleCopy,
        },
      ]}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
};

export default OperateLogDetailPage;
