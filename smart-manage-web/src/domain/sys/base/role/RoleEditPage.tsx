import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { EditField } from '@/domain/common/page/EditPage';
import { useWorkbenchStore } from '@/stores/workbench';
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
  {
    label: '默认数据范围',
    dataIndex: 'defaultDataScope',
    type: 'select',
    options: [
      { label: '全部数据', value: 'ALL' },
      { label: '本组织及下级', value: 'ORG_AND_CHILDREN' },
      { label: '本组织', value: 'ORG' },
      { label: '本人相关', value: 'SELF' },
    ],
    rules: [{ required: true, message: '请选择默认数据范围' }],
  },
];

/** 角色编辑页只维护角色资料，权限关系由专用分配页面处理。 */
const RoleEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
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
            defaultDataScope: detail.defaultDataScope,
          }
        : { defaultDataScope: 'SELF' },
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
        defaultDataScope: values.defaultDataScope as 'ALL' | 'ORG_AND_CHILDREN' | 'ORG' | 'SELF',
      });
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
      await queryClient.invalidateQueries({ queryKey: roleQueryKeys.all });
      feedback.success(isAddNew ? '新增成功' : '保存成功');
    },
  });

  return (
    <EditPage
      access={roleAccess}
      title="角色"
      fields={fields}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default RoleEditPage;
