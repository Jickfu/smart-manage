import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { executionApi } from './api';
import { executionQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '实例 ID', dataIndex: 'id', type: 'text' },
  { label: '任务 ID', dataIndex: 'jobId', type: 'text' },
  { label: '任务名称', dataIndex: 'jobName', type: 'text' },
  { label: '任务分组', dataIndex: 'jobGroup', type: 'text' },
  {
    label: '状态',
    dataIndex: 'statusLabel',
    type: 'select',
    options: [
      { label: '运行中', value: '运行中' },
      { label: '成功', value: '成功' },
      { label: '失败', value: '失败' },
    ],
  },
  { label: '耗时（毫秒）', dataIndex: 'durationMs', type: 'number' },
  { label: '开始时间', dataIndex: 'startTime', type: 'datetime' },
  { label: '结束时间', dataIndex: 'endTime', type: 'datetime' },
  { label: 'Trace ID', dataIndex: 'traceId', type: 'text', columnSpan: 2 },
  { label: '错误信息', dataIndex: 'errorMessage', type: 'textarea', fullWidth: true },
];

const ExecutionDetailPage = (props: PageComponentProps) => {
  const detailQuery = useQuery({
    queryKey: executionQueryKeys.detail(props.billId),
    queryFn: () => executionApi.detail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const initialValues = useMemo(() => {
    const detail = detailQuery.data;
    if (!detail) return {};
    return {
      ...detail,
      statusLabel: {
        RUNNING: '运行中',
        SUCCESS: '成功',
        FAILED: '失败',
        SKIPPED: '互斥跳过',
      }[detail.status],
    };
  }, [detailQuery.data]);

  return (
    <EditPage
      title="执行实例详情"
      fields={fields}
      initialValues={initialValues}
      operationType={OperationType.VIEW}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
};

export default ExecutionDetailPage;
