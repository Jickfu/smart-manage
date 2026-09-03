import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { inboxAdminApi } from './api';
import { inboxBroadcastAccess } from './permissions';
import { inboxAdminQueryKeys } from './queryKeys';
import type { InboxLevel } from './types';

const InboxMessageEditPage = (props: PageComponentProps) => {
  const { appNumber, tabKey, billId, operationType } = props;
  const isAdd = operationType === OperationType.ADDNEW;
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const detailQuery = useQuery({
    queryKey: inboxAdminQueryKeys.detail(billId),
    queryFn: () => inboxAdminApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const defaultsQuery = useQuery({
    queryKey: [...inboxAdminQueryKeys.all, 'create-new-data'],
    queryFn: inboxAdminApi.createNewData,
    enabled: isAdd,
  });
  const detail = detailQuery.data;
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '消息标题',
        dataIndex: 'title',
        type: 'text',
        fullWidth: true,
        rules: [
          { required: true, message: '消息标题不能为空' },
          { max: 200, message: '消息标题不能超过200个字符' },
        ],
      },
      {
        label: '消息级别',
        dataIndex: 'level',
        type: 'select',
        options: [
          { label: '普通', value: 'NORMAL' },
          { label: '重要', value: 'IMPORTANT' },
          { label: '紧急', value: 'URGENT' },
        ],
        rules: [{ required: true }],
      },
      {
        label: '失效时间',
        dataIndex: 'expireTime',
        type: 'datetime',
        rules: [{ required: true, message: '失效时间不能为空' }],
      },
      {
        label: '消息正文',
        dataIndex: 'content',
        type: 'textarea',
        fullWidth: true,
        rules: [
          { required: true, message: '消息正文不能为空' },
          { max: 10000, message: '消息正文不能超过10000个字符' },
        ],
      },
      ...(detail
        ? ([
            { label: '状态', dataIndex: 'status', type: 'readonly' },
            { label: '发布人', dataIndex: 'senderName', type: 'readonly' },
            { label: '收件人数', dataIndex: 'recipientCount', type: 'readonly' },
            { label: '发布时间', dataIndex: 'publishTime', type: 'readonly' },
            { label: '失败原因', dataIndex: 'errorMessage', type: 'readonly', fullWidth: true },
          ] satisfies EditField[])
        : []),
    ],
    [detail],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? detail
        : {
            level: defaultsQuery.data?.level ?? 'NORMAL',
            expireTime: defaultsQuery.data?.expireTime,
          },
    [defaultsQuery.data, detail],
  );
  const save = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) =>
      inboxAdminApi.save({
        id: billId,
        version: detail?.version,
        title: String(values.title).trim(),
        content: String(values.content).trim(),
        level: values.level as InboxLevel,
        expireTime: String(values.expireTime),
      }),
    onSuccess: async (id) => {
      if (isAdd) {
        const nextKey = createBillTabKey(props.componentKey, id);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: id,
        });
        activateContentTab(appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: inboxAdminQueryKeys.all });
      feedback.success(isAdd ? '草稿已创建' : '草稿已保存');
    },
  });
  return (
    <EditPage
      access={inboxBroadcastAccess}
      title="消息发布"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues as Record<string, unknown>}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading || defaultsQuery.isLoading}
      error={(detailQuery.error ?? defaultsQuery.error) as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), defaultsQuery.refetch()])}
      onSave={async (values) => {
        await save.mutateAsync(values);
      }}
      saving={save.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default InboxMessageEditPage;
