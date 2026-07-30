import { useMemo } from 'react';
import { App } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
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
];

/** 角色编辑页只维护角色资料，权限关系由专用分配页面处理。 */
const RoleEditPage = (props: PageComponentProps) => {
  const { message } = App.useApp();
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
    () => (detail ? { number: detail.number ?? '', name: detail.name ?? '' } : {}),
    [detail],
  );
  const saveMutation = useMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const name = (values.name as string).trim();
      const savedId = await roleApi.save({
        id: billId ?? undefined,
        version: detail?.version,
        name,
        number: (values.number as string).trim(),
      });
      if (isAddNew) {
        const nextKey = `bill:${props.componentKey}:${savedId}`;
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          label: name,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: roleQueryKeys.all });
      message.success(isAddNew ? '新增成功' : '保存成功');
    },
  });

  return (
    <EditPage
      access={roleAccess}
      title="角色管理"
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
