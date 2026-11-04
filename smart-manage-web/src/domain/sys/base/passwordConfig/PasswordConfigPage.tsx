import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import EditPage, { type EditField } from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { OperationType, type PageComponentProps } from '@/domain/common/page/types';
import { passwordConfigApi } from './api';
import { passwordConfigAccess } from './permissions';
import { passwordConfigQueryKeys } from './queryKeys';
import { passwordPolicyQueryKey } from '@/security/usePasswordPolicy';

const fields: EditField[] = [
  {
    label: '密码最小长度',
    dataIndex: 'minLength',
    type: 'number',
    tooltip: '允许设置为4～64位；长度过短会降低安全性。',
    rules: [{ required: true, type: 'integer', min: 4, max: 64, message: '请输入4～64的整数' }],
  },
  {
    label: '启用密码复杂度',
    dataIndex: 'complexityEnabled',
    type: 'switch',
    tooltip: '启用后必须同时包含大写字母、小写字母、数字和特殊字符。',
  },
];

export default function PasswordConfigPage({ appNumber, tabKey }: PageComponentProps) {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: passwordConfigQueryKeys.singleton,
    queryFn: passwordConfigApi.singleton,
    // 保留正在编辑的快照；重新联网或切回窗口时不覆盖未保存内容，冲突交由版本检查处理。
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
    meta: { errorPresentation: 'local-initial' },
  });
  const initialValues = useMemo(() => (query.data ? { ...query.data } : {}), [query.data]);
  const saveMutation = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) =>
      passwordConfigApi.save({
        id: query.data!.id,
        version: query.data!.version,
        minLength: values.minLength as number,
        complexityEnabled: values.complexityEnabled as boolean,
      }),
    onSuccess: async () => {
      feedback.success('密码策略已保存');
      // 远端已提交；回显失败单独提示，避免把刷新失败误报成保存失败。
      const results = await Promise.allSettled([
        queryClient.invalidateQueries(
          { queryKey: passwordConfigQueryKeys.all },
          { throwOnError: true },
        ),
        queryClient.invalidateQueries({ queryKey: passwordPolicyQueryKey }, { throwOnError: true }),
      ]);
      if (results.some((result) => result.status === 'rejected'))
        feedback.warning('密码策略已保存，刷新失败，请重新打开页面');
    },
  });
  return (
    <EditPage
      title="密码策略"
      access={passwordConfigAccess}
      sections={[editFormSection('policy', '密码要求', fields)]}
      initialValues={initialValues}
      operationType={OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={query.isLoading}
      error={getBlockingQueryError(query) as Error | null}
      onRetry={() => query.refetch()}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
      headerActions={[{ builtin: 'save' }]}
    />
  );
}
