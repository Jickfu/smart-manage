import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataOption, BasicDataSaveForm } from './types';

interface ParentTreeNode {
  value: string;
  title: string;
  children: ParentTreeNode[];
}

function buildParentTree(options: BasicDataOption[]): ParentTreeNode[] {
  const nodes = new Map(
    options.map((option) => [
      option.id,
      {
        value: option.id,
        title: `${option.number} - ${option.name}`,
        children: [] as ParentTreeNode[],
      },
    ]),
  );
  const roots: ParentTreeNode[] = [];
  for (const option of options) {
    const node = nodes.get(option.id)!;
    const parent = option.parentId ? nodes.get(option.parentId) : undefined;
    if (parent) parent.children.push(node);
    else roots.push(node);
  }
  return roots;
}

const BasicDataEditPage = (props: PageComponentProps) => {
  const { appNumber, tabKey, billId, operationType, context } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: basicDataQueryKeys.detail(billId),
    queryFn: () => basicDataApi.detail(billId!),
    enabled: Boolean(!isAddNew && billId),
  });
  const categoryId = detailQuery.data?.categoryId ?? context?.categoryId;
  const categoryQuery = useQuery({
    queryKey: basicDataQueryKeys.category(categoryId),
    queryFn: () => basicDataApi.categoryDetail(categoryId!),
    enabled: Boolean(categoryId),
  });
  const parentsQuery = useQuery({
    queryKey: basicDataQueryKeys.parentOptions(categoryId, billId),
    queryFn: () => basicDataApi.parentOptions(categoryId!, billId),
    enabled: Boolean(categoryId),
  });
  const detail = detailQuery.data;
  const numberMode = categoryQuery.data?.numberMode ?? 'AUTO_DEFAULT';
  const fields = useMemo<EditField[]>(
    () => [
      { label: '所属分类', dataIndex: 'categoryName', type: 'readonly' },
      {
        label: '上级基础资料',
        dataIndex: 'parentId',
        type: 'tree-select',
        treeData: buildParentTree(parentsQuery.data ?? []),
        placeholder: '不选择则为一级资料',
      },
      {
        label: '编码',
        dataIndex: 'number',
        type: 'text',
        disabled: numberMode === 'AUTO_LOCKED',
        placeholder:
          numberMode === 'AUTO_DEFAULT'
            ? '留空则保存时自动生成'
            : numberMode === 'AUTO_LOCKED'
              ? '保存时自动生成'
              : undefined,
        rules: [
          {
            required: numberMode === 'MANUAL' || !isAddNew,
            whitespace: true,
            message: '编码不能为空',
          },
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
      { label: '排序', dataIndex: 'sort', type: 'number' },
      { label: '可用状态', dataIndex: 'enabled', type: 'switch' },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [isAddNew, numberMode, parentsQuery.data],
  );
  const initialValues = useMemo(
    () => ({
      categoryName: detail?.categoryName ?? categoryQuery.data?.name ?? '',
      parentId: detail?.parentId,
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      sort: detail?.sort ?? 0,
      enabled: detail?.enabled ?? true,
      description: detail?.description ?? '',
    }),
    [categoryQuery.data, detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      if (!categoryId) throw new Error('未指定基础资料分类');
      const form: BasicDataSaveForm = {
        id: billId,
        version: detail?.version,
        categoryId,
        parentId: values.parentId ? String(values.parentId) : undefined,
        number: values.number ? String(values.number).trim() : undefined,
        name: String(values.name).trim(),
        description: values.description ? String(values.description).trim() : undefined,
        sort: Number(values.sort ?? 0),
        enabled: Boolean(values.enabled),
      };
      const savedId = await basicDataApi.save(form);
      await queryClient.invalidateQueries({ queryKey: basicDataQueryKeys.all });
      if (isAddNew) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
    },
    successMessage: isAddNew ? '新增成功' : '保存成功',
  });

  return (
    <EditPage
      access={basicDataAccess}
      title="基础资料"
      fields={fields}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading || categoryQuery.isLoading || parentsQuery.isLoading}
      error={(detailQuery.error ?? categoryQuery.error ?? parentsQuery.error) as Error | null}
      onRetry={() =>
        Promise.all([detailQuery.refetch(), categoryQuery.refetch(), parentsQuery.refetch()])
      }
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default BasicDataEditPage;
