import { useMemo } from 'react';
import { App } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { EditField } from '@/domain/common/page/EditPage';
import { useWorkbenchStore } from '@/stores/workbench';
import { userApi } from './api';
import { userAccess } from './permissions';
import { userQueryKeys } from './queryKeys';
import type { PageComponentProps } from '@/domain/common/page/types';

/** 用户编辑页只维护用户资料，角色关系由专用分配页面处理。 */
const UserEditPage = (props: PageComponentProps) => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: userQueryKeys.detail(billId),
    queryFn: () => userApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const detail = detailQuery.data;
  const fields: EditField[] = useMemo(
    () => [
      {
        label: '用户名',
        dataIndex: 'username',
        type: 'text',
        disabled: !isAddNew,
        rules: [{ required: true, message: '用户名不能为空' }],
      },
      ...(isAddNew
        ? [
            {
              label: '密码',
              dataIndex: 'password',
              type: 'password' as const,
              placeholder: '请输入初始密码',
              rules: [{ required: true, message: '密码不能为空' }],
            },
          ]
        : []),
      { label: '昵称', dataIndex: 'nickname', type: 'text' },
      {
        label: '邮箱',
        dataIndex: 'email',
        type: 'text',
        rules: [{ type: 'email', message: '邮箱格式不正确' }],
      },
      {
        label: '手机号',
        dataIndex: 'phone',
        type: 'text',
        rules: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }],
      },
      { label: '头像URL', dataIndex: 'avatar', type: 'text' },
      { label: '创建时间', dataIndex: 'createTime', type: 'datetime', disabled: true },
      { label: '更新时间', dataIndex: 'updateTime', type: 'datetime', disabled: true },
    ],
    [isAddNew],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? {
            username: detail.username ?? '',
            nickname: detail.nickname ?? '',
            email: detail.email ?? '',
            phone: detail.phone ?? '',
            avatar: detail.avatar ?? '',
            createTime: detail.createTime ?? '',
            updateTime: detail.updateTime ?? '',
          }
        : {},
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const username = (values.username as string).trim();
      const savedId = await userApi.save({
        id: billId ?? undefined,
        version: detail?.version,
        username,
        password: (values.password as string) || undefined,
        nickname: (values.nickname as string) ?? undefined,
        email: ((values.email as string) ?? '').trim() || undefined,
        phone: ((values.phone as string) ?? '').trim() || undefined,
        avatar: (values.avatar as string) ?? undefined,
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
      await queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
      message.success(isAddNew ? '新增成功' : '保存成功');
    },
  });

  return (
    <EditPage
      access={userAccess}
      title="用户"
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

export default UserEditPage;
