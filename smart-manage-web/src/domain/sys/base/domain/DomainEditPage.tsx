import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { domainApi } from './api';
import { domainAccess } from './permissions';
import { domainQueryKeys } from './queryKeys';

interface Props {
  open: boolean;
  domainId: string | null;
  onClose: () => void;
  onSaved: () => void;
}

/** 领域编辑字段定义 */
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
  { label: '排序', dataIndex: 'seq', type: 'number' },
];

/** 领域编辑弹框 */
const DomainEditPage = ({ open, domainId, onClose, onSaved }: Props) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const isAddNew = domainId === null;

  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: domainQueryKeys.detail(domainId),
    queryFn: () => domainApi.detail(domainId!),
    enabled: Boolean(open && domainId),
    staleTime: 0,
  });

  const detail = detailQuery.data;

  // Form 初始值，从详情数据派生
  const initialValues = useMemo(() => {
    if (!detail) return {};
    return {
      number: detail.number ?? '',
      name: detail.name ?? '',
      seq: detail.seq ?? undefined,
    };
  }, [detail]);

  const handleSave = async (values: Record<string, unknown>) => {
    await domainApi.save({
      id: domainId ?? undefined,
      version: detail?.version,
      name: (values.name as string).trim(),
      number: (values.number as string).trim(),
      seq: (values.seq as number) ?? 0,
    });
    await queryClient.invalidateQueries({ queryKey: domainQueryKeys.all });
    feedback.success(isAddNew ? '新增成功' : '保存成功');
    onSaved();
  };
  const saveMutation = useCommandMutation({
    mutationFn: handleSave,
  });

  return (
    <ModalEditPage
      access={domainAccess}
      title="领域"
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => detailQuery.refetch()}
    />
  );
};

export default DomainEditPage;
