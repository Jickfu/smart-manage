import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useEditTabLifecycle } from '@/domain/common/page/edit/useEditTabLifecycle';
import {
  getEditSavePostCommitFeedback,
  runEditSavePostCommit,
} from '@/domain/common/page/edit/editSavePostCommit';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import { OperationType } from '@/domain/common/page/types';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { roleApi } from './api';
import { roleAccess } from './permissions';
import { roleQueryKeys } from './queryKeys';
import type { PageComponentProps } from '@/domain/common/page/types';

const fields: EditField[] = [
  {
    label: '编码',
    dataIndex: 'number',
    type: 'text',
    rules: [{ required: true, message: '编码不能为空' }],
  },
  {
    label: '名称',
    dataIndex: 'name',
    type: 'text',
    rules: [{ required: true, message: '名称不能为空' }],
  },
  {
    label: '描述',
    dataIndex: 'description',
    type: 'textarea',
    fullWidth: true,
  },
];

/** 角色编辑页只维护角色资料，权限关系由专用分配页面处理。 */
const RoleEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const { isAddNew, promoteToPersistedTab, exit } = useEditTabLifecycle(props);
  const detailQuery = useQuery({
    queryKey: roleQueryKeys.detail(billId),
    queryFn: () => roleApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () =>
      detail
        ? {
            number: detail.number ?? '',
            name: detail.name ?? '',
            description: detail.description ?? '',
          }
        : {},
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const name = (values.name as string).trim();
      const savedId = await roleApi.save({
        id: billId ?? undefined,
        version: detail?.version,
        name,
        number: (values.number as string).trim(),
        description: String(values.description ?? '').trim(),
      });
      return savedId;
    },
    onSuccess: async (savedId) => {
      const result = await runEditSavePostCommit({
        syncTab: () => promoteToPersistedTab(savedId),
        refreshCache: () =>
          queryClient.invalidateQueries({ queryKey: roleQueryKeys.all }, { throwOnError: true }),
      });
      const resultFeedback = getEditSavePostCommitFeedback(result, isAddNew);
      feedback[resultFeedback.type](resultFeedback.message);
    },
  });

  return (
    <EditPage
      access={roleAccess}
      title="角色"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
      onExit={exit}
    />
  );
};

export default RoleEditPage;
