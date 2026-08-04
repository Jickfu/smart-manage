import { useQuery } from '@tanstack/react-query';
import { App } from 'antd';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { sqlApi } from './api';
import { sqlQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '日志 ID', dataIndex: 'id', type: 'text' },
  { label: '结果类型', dataIndex: 'resultType', type: 'text' },
  { label: '影响/返回行数', dataIndex: 'rowCount', type: 'number' },
  { label: '执行耗时（ms）', dataIndex: 'executeDuration', type: 'number' },
  { label: '执行人', dataIndex: 'createName', type: 'text' },
  { label: '执行 IP', dataIndex: 'createIp', type: 'text' },
  { label: '执行时间', dataIndex: 'createTime', type: 'datetime' },
  { label: 'SQL', dataIndex: 'sqlText', type: 'textarea', fullWidth: true },
  { label: '错误信息', dataIndex: 'errorMessage', type: 'textarea', fullWidth: true },
];

export default function SqlLogDetailPage(props: PageComponentProps) {
  const { message } = App.useApp();
  const query = useQuery({
    queryKey: sqlQueryKeys.logDetail(props.billId),
    queryFn: () => sqlApi.detail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const handleCopySql = async () => {
    const sqlText = query.data?.sqlText;
    if (!sqlText) return;
    try {
      await navigator.clipboard.writeText(sqlText);
      message.success('SQL 已复制');
    } catch {
      message.error('复制失败，请检查浏览器剪贴板权限');
    }
  };
  return (
    <EditPage
      title="SQL 执行详情"
      fields={fields}
      initialValues={query.data ? { ...query.data } : {}}
      operationType={OperationType.VIEW}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      headerActions={[
        {
          key: 'copySql',
          label: '复制 SQL',
          disabled: !query.data?.sqlText,
          onClick: handleCopySql,
        },
      ]}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
