import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/ModalEditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { cloudApi } from '@/domain/sys/base/cloud/api';
import { cloudQueryKeys } from '@/domain/sys/base/cloud/queryKeys';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataCategorySaveForm } from './types';

interface Props {
  open: boolean;
  categoryId: string | null;
  cloudId?: string;
  onClose: () => void;
  onSaved: () => void;
}

const BasicDataCategoryEditModal = ({ open, categoryId, cloudId, onClose, onSaved }: Props) => {
  const detailQuery = useQuery({
    queryKey: basicDataQueryKeys.category(categoryId),
    queryFn: () => basicDataApi.categoryDetail(categoryId!),
    enabled: Boolean(open && categoryId),
    staleTime: 0,
  });
  const cloudsQuery = useQuery({
    queryKey: [...cloudQueryKeys.lists(), 'basic-data-category'],
    queryFn: () => cloudApi.select({ pageNum: 1, pageSize: 1000 }),
    enabled: open,
  });
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '编码',
        dataIndex: 'number',
        type: 'text',
        rules: [
          { required: true, whitespace: true, message: '编码不能为空' },
          { max: 64, message: '编码不能超过64个字符' },
        ],
      },
      {
        label: '名称',
        dataIndex: 'name',
        type: 'text',
        rules: [
          { required: true, whitespace: true, message: '名称不能为空' },
          { max: 128, message: '名称不能超过128个字符' },
        ],
      },
      {
        label: '所属云',
        dataIndex: 'cloudId',
        type: 'select',
        disabled: true,
        options: (cloudsQuery.data?.records ?? []).map((cloud) => ({
          label: cloud.name,
          value: cloud.id,
        })),
        rules: [{ required: true, message: '所属云不能为空' }],
      },
      { label: '可用状态', dataIndex: 'enabled', type: 'switch' },
      { label: '备注', dataIndex: 'remark', type: 'textarea', fullWidth: true },
    ],
    [cloudsQuery.data],
  );
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () => ({
      cloudId: detail?.cloudId ?? cloudId,
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      enabled: detail?.enabled ?? true,
      remark: detail?.remark ?? '',
    }),
    [cloudId, detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const form: BasicDataCategorySaveForm = {
        id: categoryId ?? undefined,
        version: detail?.version,
        cloudId: String(values.cloudId),
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        enabled: Boolean(values.enabled),
        remark: values.remark ? String(values.remark).trim() : undefined,
      };
      await basicDataApi.saveCategory(form);
      onSaved();
    },
    successMessage: categoryId ? '分类保存成功' : '分类新增成功',
  });

  return (
    <ModalEditPage
      title={categoryId ? '编辑基础资料分类' : '新增基础资料分类'}
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading || cloudsQuery.isLoading}
      error={(detailQuery.error ?? cloudsQuery.error) as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), cloudsQuery.refetch()])}
      access={basicDataAccess}
    />
  );
};

export default BasicDataCategoryEditModal;
