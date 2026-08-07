import { App } from 'antd';
import { useQuery } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { scriptApi } from './api';
import { scriptQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '日志 ID', dataIndex: 'id', type: 'text' },
  { label: '关联脚本', dataIndex: 'scriptName', type: 'text' },
  { label: '执行状态', dataIndex: 'executeStatus', type: 'text' },
  { label: '执行模式', dataIndex: 'transactionMode', type: 'text' },
  { label: '事务结果', dataIndex: 'transactionResult', type: 'text' },
  { label: '执行耗时（ms）', dataIndex: 'executeDuration', type: 'number' },
  { label: '执行人', dataIndex: 'createName', type: 'text' },
  { label: '执行 IP', dataIndex: 'createIp', type: 'text' },
  { label: '执行时间', dataIndex: 'createTime', type: 'datetime' },
  { label: '脚本快照', dataIndex: 'scriptContent', type: 'textarea', fullWidth: true },
  { label: '输出', dataIndex: 'output', type: 'textarea', fullWidth: true },
  { label: '错误信息', dataIndex: 'errorMessage', type: 'textarea', fullWidth: true },
];

export default function ScriptLogDetailPage(props: PageComponentProps) {
  const { message } = App.useApp();
  const query = useQuery({
    queryKey: scriptQueryKeys.logDetail(props.billId),
    queryFn: () => scriptApi.logDetail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const copy = async (text: string | undefined, successMessage: string) => {
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      message.success(successMessage);
    } catch {
      message.error('复制失败，请检查浏览器剪贴板权限');
    }
  };
  return (
    <EditPage
      title="脚本执行记录"
      fields={fields}
      initialValues={query.data ? { ...query.data } : {}}
      operationType={OperationType.VIEW}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      headerActions={[
        {
          key: 'copyScript',
          label: '复制脚本',
          disabled: !query.data?.scriptContent,
          onClick: () => copy(query.data?.scriptContent, '脚本已复制'),
        },
        {
          key: 'copyOutput',
          label: '复制输出',
          disabled: !query.data?.output,
          onClick: () => copy(query.data?.output, '输出已复制'),
        },
      ]}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
