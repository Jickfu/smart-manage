import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { domainApi } from '@/domain/sys/base/domain/api';
import { domainQueryKeys } from '@/domain/sys/base/domain/queryKeys';
import { numberRuleQueryKeys } from '@/domain/sys/base/numberRule/queryKeys';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataCategorySaveForm } from './types';

interface Props {
  open: boolean;
  categoryId: string | null;
  domainId?: string;
  onClose: () => void;
  onSaved: () => void;
}

const BasicDataCategoryEditModal = ({ open, categoryId, domainId, onClose, onSaved }: Props) => {
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: basicDataQueryKeys.category(categoryId),
    queryFn: () => basicDataApi.categoryDetail(categoryId!),
    enabled: Boolean(open && categoryId),
    staleTime: 0,
  });
  const domainsQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: [...domainQueryKeys.lists(), 'basic-data-category'],
    queryFn: () => domainApi.select({ pageNum: 1, pageSize: 1000 }),
    enabled: open,
  });
  const numberRulesQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: numberRuleQueryKeys.options('CATEGORY'),
    queryFn: basicDataApi.numberRuleOptions,
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
        label: '所属领域',
        dataIndex: 'domainId',
        type: 'select',
        disabled: true,
        options: (domainsQuery.data?.records ?? []).map((domain) => ({
          label: domain.name,
          value: domain.id,
        })),
        rules: [{ required: true, message: '所属领域不能为空' }],
      },
      {
        label: '节点编号模式',
        dataIndex: 'numberMode',
        type: 'select',
        options: [
          { label: '留空自动生成，可修改', value: 'AUTO_DEFAULT' },
          { label: '自动生成并锁定', value: 'AUTO_LOCKED' },
          { label: '人工填写', value: 'MANUAL' },
        ],
        rules: [{ required: true, message: '请选择节点编号模式' }],
      },
      {
        label: '节点编号规则',
        dataIndex: 'numberRuleKey',
        type: 'select',
        options: (numberRulesQuery.data ?? []).map((rule) => ({
          label: `${rule.name}（${rule.pattern}）`,
          value: rule.ruleKey,
        })),
        rules: [{ required: true, message: '请选择节点编号规则' }],
      },
      { label: '可用状态', dataIndex: 'enabled', type: 'switch' },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [domainsQuery.data, numberRulesQuery.data],
  );
  const detail = detailQuery.data;
  const initialValues = useMemo(
    () => ({
      domainId: detail?.domainId ?? domainId,
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      enabled: detail?.enabled ?? true,
      numberMode: detail?.numberMode ?? 'AUTO_DEFAULT',
      numberRuleKey:
        detail?.numberRuleKey ??
        numberRulesQuery.data?.find((rule) => rule.defaultRule)?.ruleKey ??
        'sys/base/basic-data-item',
      description: detail?.description ?? '',
    }),
    [domainId, detail, numberRulesQuery.data],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const form: BasicDataCategorySaveForm = {
        id: categoryId ?? undefined,
        version: detail?.version,
        domainId: String(values.domainId),
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        enabled: Boolean(values.enabled),
        numberMode: values.numberMode as BasicDataCategorySaveForm['numberMode'],
        numberRuleKey: String(values.numberRuleKey),
        description: values.description ? String(values.description).trim() : undefined,
      };
      await basicDataApi.saveCategory(form);
      onSaved();
    },
    successMessage: categoryId ? '分类保存成功' : '分类新增成功',
  });

  return (
    <ModalEditPage
      title="基础资料分类"
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      loading={detailQuery.isLoading || domainsQuery.isLoading || numberRulesQuery.isLoading}
      error={
        (getBlockingQueryError(detailQuery) ??
          getBlockingQueryError(domainsQuery) ??
          getBlockingQueryError(numberRulesQuery)) as Error | null
      }
      onRetry={() =>
        Promise.all([detailQuery.refetch(), domainsQuery.refetch(), numberRulesQuery.refetch()])
      }
      access={basicDataAccess}
    />
  );
};

export default BasicDataCategoryEditModal;
