import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/edit/EditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { emailApi } from './api';
import { accountAccess } from './permissions';
import { emailAccountQueryKeys } from './queryKeys';
import type { AccountForm } from './types';

const EmailAccountEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, billId, operationType } = props;
  const isAdd = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((s) => s.replaceContentTab);
  const activateContentTab = useWorkbenchStore((s) => s.activateContentTab);
  const detailQuery = useQuery({
    queryKey: emailAccountQueryKeys.detail(billId),
    queryFn: () => emailApi.accountDetail(billId!),
    enabled: !!billId,
  });
  const detail = detailQuery.data;
  const basicFields = useMemo<EditField[]>(
    () => [
      {
        label: '账号编码',
        dataIndex: 'number',
        type: 'text',
        rules: [{ required: true, message: '账号编码不能为空' }],
      },
      {
        label: '账号名称',
        dataIndex: 'name',
        type: 'text',
        rules: [{ required: true, message: '账号名称不能为空' }],
      },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [],
  );
  const emailFields = useMemo<EditField[]>(
    () => [
      {
        label: '安全模式',
        dataIndex: 'securityMode',
        type: 'select',
        options: [
          { label: '无加密', value: 'NONE' },
          { label: 'STARTTLS', value: 'STARTTLS' },
          { label: 'SSL/TLS', value: 'SSL_TLS' },
        ],
        rules: [{ required: true }],
      },
      { label: 'SMTP 主机', dataIndex: 'host', type: 'text', rules: [{ required: true }] },
      { label: '端口', dataIndex: 'port', type: 'number', rules: [{ required: true }] },
      { label: '登录用户名', dataIndex: 'username', type: 'text', rules: [{ required: true }] },
      {
        label: '密码/授权码',
        dataIndex: 'password',
        type: 'password',
        placeholder: detail?.passwordConfigured ? '已配置，留空保留原凭据' : '首次保存必须填写',
        rules: [
          {
            validator: (_, value) =>
              detail?.passwordConfigured || value
                ? Promise.resolve()
                : Promise.reject(new Error('密码或授权码不能为空')),
          },
        ],
      },
      {
        label: '发件地址',
        dataIndex: 'fromAddress',
        type: 'text',
        rules: [{ required: true }, { type: 'email', message: '邮箱格式不正确' }],
      },
      { label: '发件人名称', dataIndex: 'fromName', type: 'text' },
      {
        label: 'Reply-To',
        dataIndex: 'replyTo',
        type: 'text',
        rules: [{ type: 'email', message: '邮箱格式不正确' }],
      },
      {
        label: '连接超时（毫秒）',
        dataIndex: 'connectionTimeoutMs',
        type: 'number',
        rules: [{ required: true }],
      },
      {
        label: '读取超时（毫秒）',
        dataIndex: 'readTimeoutMs',
        type: 'number',
        rules: [{ required: true }],
      },
      { label: '全局默认', dataIndex: 'defaultAccount', type: 'switch' },
      { label: '允许手工选择', dataIndex: 'allowManual', type: 'switch' },
    ],
    [detail?.passwordConfigured],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? { ...detail, password: '' }
        : {
            port: 587,
            securityMode: 'STARTTLS',
            defaultAccount: false,
            allowManual: true,
            connectionTimeoutMs: 10000,
            readTimeoutMs: 10000,
          },
    [detail],
  );
  const save = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) =>
      emailApi.accountSave({
        ...values,
        id: billId,
        version: detail?.version,
        password: String(values.password ?? '').trim() || undefined,
      } as AccountForm),
    onSuccess: async (id) => {
      if (isAdd) {
        const next = createBillTabKey(props.componentKey, id);
        replaceContentTab(appNumber, tabKey, {
          key: next,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: id,
        });
        activateContentTab(appNumber, next);
      }
      await queryClient.invalidateQueries({ queryKey: emailAccountQueryKeys.all });
      feedback.success(isAdd ? '新增成功' : '保存成功');
    },
  });
  return (
    <EditPage
      access={accountAccess}
      title="发信账号"
      sections={[
        {
          key: 'basic',
          label: '基本信息',
          content: (editable) => <EditFormFields fields={basicFields} editable={editable} />,
        },
        {
          key: 'email-config',
          label: '邮箱配置',
          content: (editable) => <EditFormFields fields={emailFields} editable={editable} />,
        },
      ]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={async (values) => {
        await save.mutateAsync(values);
      }}
      saving={save.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};
export default EmailAccountEditPage;
