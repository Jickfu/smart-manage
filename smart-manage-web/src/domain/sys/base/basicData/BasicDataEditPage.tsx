import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataSaveForm } from './types';
import { useBasicDataRefSelector } from './refSelector';

const BasicDataEditPage = (props: PageComponentProps) => {
  const { appNumber, tabKey, billId, operationType, context } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    // 新增页使用临时页签标识隔离查询状态，避免复用 detail(undefined) 的历史错误缓存。
    queryKey: basicDataQueryKeys.detail(isAddNew ? tabKey : billId),
    queryFn: () => basicDataApi.detail(billId!),
    enabled: Boolean(!isAddNew && billId),
  });
  const categoryId = detailQuery.data?.category.id ?? context?.categoryId;
  const categoryQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: basicDataQueryKeys.category(categoryId),
    queryFn: () => basicDataApi.categoryDetail(categoryId!),
    enabled: Boolean(categoryId),
  });
  const parentRefSelector = useBasicDataRefSelector(categoryQuery.data, billId);
  const detail = detailQuery.data;
  const numberMode = categoryQuery.data?.numberMode ?? 'AUTO_DEFAULT';
  const fields = useMemo<EditField[]>(
    () => [
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
      {
        label: '所属分类',
        dataIndex: 'category',
        type: 'ref-selector',
        disabled: true,
        refSelector: {
          selectorKey: ['sys-base-basic-data-category', categoryId],
          modalTitle: '所属分类',
          fetchFn: async () => ({ records: [], total: 0 }),
          displayRender: (record) => String(record.name ?? ''),
          fieldNames: { key: 'id', label: 'name' },
          columns: [
            { title: '编码', dataIndex: 'number', width: 160 },
            { title: '名称', dataIndex: 'name' },
          ],
        },
      },
      {
        label: '上级基础资料',
        dataIndex: 'parent',
        type: 'ref-selector',
        refSelector: parentRefSelector,
        placeholder: '不选择则为一级资料',
      },
      { label: '排序', dataIndex: 'sort', type: 'number' },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [categoryId, isAddNew, numberMode, parentRefSelector],
  );
  const initialValues = useMemo(
    () => ({
      number: detail?.number ?? '',
      name: detail?.name ?? '',
      category: categoryQuery.data ?? null,
      parent: detail?.parent ?? null,
      sort: detail?.sort ?? 0,
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
        parentId: (values.parent as { id?: string } | null)?.id,
        number: values.number ? String(values.number).trim() : undefined,
        name: String(values.name).trim(),
        description: values.description ? String(values.description).trim() : undefined,
        sort: Number(values.sort ?? 0),
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
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading || categoryQuery.isLoading}
      error={
        ((!isAddNew ? getBlockingQueryError(detailQuery) : null) ??
          getBlockingQueryError(categoryQuery)) as Error | null
      }
      onRetry={() =>
        Promise.all([
          ...(!isAddNew && billId ? [detailQuery.refetch()] : []),
          ...(categoryId ? [categoryQuery.refetch()] : []),
        ])
      }
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default BasicDataEditPage;
