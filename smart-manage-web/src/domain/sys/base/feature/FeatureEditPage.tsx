import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { featureApi } from './api';
import { featureAccess } from './permissions';
import { featureQueryKeys } from './queryKeys';

interface Props {
  open: boolean;
  featureId: string | null;
  onClose: () => void;
}

const FeatureEditPage = ({ open, featureId, onClose }: Props) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: featureQueryKeys.detail(featureId),
    queryFn: () => featureApi.detail(featureId!),
    enabled: Boolean(open && featureId),
  });
  const fields = useMemo<EditField[]>(
    () => [
      { label: '功能键', dataIndex: 'featureKey', type: 'text', disabled: true },
      { label: '所属应用', dataIndex: 'appName', type: 'text', disabled: true },
      { label: '来源', dataIndex: 'source', type: 'text', disabled: true },
      { label: '默认名称', dataIndex: 'defaultName', type: 'text', disabled: true },
      {
        label: '自定义名称',
        dataIndex: 'customName',
        type: 'text',
        placeholder: '留空使用默认名称',
      },
      { label: '默认排序', dataIndex: 'defaultSeq', type: 'number', disabled: true },
      {
        label: '自定义排序',
        dataIndex: 'customSeq',
        type: 'number',
        placeholder: '留空使用默认排序',
      },
      { label: '目录可见', dataIndex: 'visible', type: 'switch' },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (!query.data) return;
      await featureApi.save({
        id: query.data.id,
        version: query.data.version,
        customName: values.customName as string | undefined,
        customSeq: values.customSeq as number | undefined,
        description: values.description as string | undefined,
        visible: Boolean(values.visible),
      });
      await queryClient.invalidateQueries({ queryKey: featureQueryKeys.all });
      feedback.success('保存成功');
      onClose();
    },
  });
  return (
    <ModalEditPage
      title="功能"
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={query.data ? { ...query.data } : undefined}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      access={featureAccess}
    />
  );
};

export default FeatureEditPage;
