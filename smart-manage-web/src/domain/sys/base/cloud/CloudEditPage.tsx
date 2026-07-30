import { useMemo } from 'react';
import { App } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/ModalEditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { cloudApi } from './api';
import { cloudAccess } from './permissions';
import { cloudQueryKeys } from './queryKeys';

interface Props {
  open: boolean;
  cloudId: string | null;
  onClose: () => void;
  onSaved: () => void;
}

/** 云编辑字段定义 */
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

/** 云编辑弹框 */
const CloudEditPage = ({ open, cloudId, onClose, onSaved }: Props) => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const isAddNew = cloudId === null;

  const detailQuery = useQuery({
    queryKey: cloudQueryKeys.detail(cloudId),
    queryFn: () => cloudApi.detail(cloudId!),
    enabled: Boolean(open && cloudId),
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
    await cloudApi.save({
      id: cloudId ?? undefined,
      version: detail?.version,
      name: (values.name as string).trim(),
      number: (values.number as string).trim(),
      seq: (values.seq as number) ?? 0,
    });
    await queryClient.invalidateQueries({ queryKey: cloudQueryKeys.all });
    message.success(isAddNew ? '新增成功' : '保存成功');
    onSaved();
  };
  const saveMutation = useMutation({
    mutationFn: handleSave,
    onError: (error) => message.error(error instanceof Error ? error.message : '保存失败'),
  });

  return (
    <ModalEditPage
      access={cloudAccess}
      title={isAddNew ? '新增云' : '编辑云'}
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

export default CloudEditPage;
