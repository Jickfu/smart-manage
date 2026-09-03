import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useFeatureRefSelector } from '@/domain/sys/base/feature/refSelector/index';
import { permissionApi } from './api';
import { permissionAccess } from './permissions';
import { permissionQueryKeys } from './queryKeys';

interface Props {
  open: boolean;
  permissionId: string | null;
  onClose: () => void;
  onSaved: () => void;
}

const PermissionEditPage = ({ open, permissionId, onClose, onSaved }: Props) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const isAddNew = permissionId === null;
  const featureRefSelector = useFeatureRefSelector();
  const fields = useMemo<EditField[]>(
    () => [
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
        label: '所属功能',
        dataIndex: 'feature',
        type: 'ref-selector',
        rules: [{ required: true, message: '所属功能不能为空' }],
        refSelector: featureRefSelector,
      },
    ],
    [featureRefSelector],
  );

  const detailQuery = useQuery({
    queryKey: permissionQueryKeys.detail(permissionId),
    queryFn: () => permissionApi.detail(permissionId!),
    enabled: Boolean(open && permissionId),
    staleTime: 0,
  });
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () =>
      detail
        ? {
            number: detail.number ?? '',
            name: detail.name ?? '',
            feature: detail.feature,
          }
        : {},
    [detail],
  );

  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const feature = values.feature as { id: string } | null;
      if (!feature?.id) throw new Error('所属功能不能为空');
      await permissionApi.save({
        id: permissionId ?? undefined,
        version: detail?.version,
        name: (values.name as string).trim(),
        number: (values.number as string).trim(),
        featureId: feature.id,
      });
      await queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all });
      feedback.success(isAddNew ? '新增成功' : '保存成功');
      onSaved();
    },
  });

  return (
    <ModalEditPage
      access={permissionAccess}
      title="权限定义"
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
    />
  );
};

export default PermissionEditPage;
