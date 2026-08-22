import { useMemo } from 'react';
import { App } from 'antd';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useUserRefSelector } from '@/domain/sys/base/user/refSelector/useUserRefSelector';
import { useEmailAccountRefSelector } from './refSelector/useEmailAccountRefSelector';
import { emailApi } from './api';
import { composeAccess } from './permissions';

const EmailComposePage = (props: PageComponentProps) => {
  const { message, modal } = App.useApp();
  const accountSelector = useEmailAccountRefSelector();
  const recipientSelector = useUserRefSelector({ multiple: true, title: '选择收件人' });
  const ccSelector = useUserRefSelector({ multiple: true, title: '选择抄送人' });
  const bccSelector = useUserRefSelector({ multiple: true, title: '选择密送人' });
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '发信账号',
        dataIndex: 'account',
        type: 'ref-selector',
        refSelector: accountSelector,
        placeholder: '留空时使用全局默认账号',
      },
      {
        label: '收件人',
        dataIndex: 'toUsers',
        type: 'ref-selector',
        refSelector: recipientSelector,
        rules: [{ required: true, message: '请选择收件人' }],
        fullWidth: true,
      },
      {
        label: '抄送',
        dataIndex: 'ccUsers',
        type: 'ref-selector',
        refSelector: ccSelector,
        fullWidth: true,
      },
      {
        label: '密送',
        dataIndex: 'bccUsers',
        type: 'ref-selector',
        refSelector: bccSelector,
        fullWidth: true,
      },
      {
        label: '主题',
        dataIndex: 'subject',
        type: 'text',
        rules: [{ required: true, max: 300 }],
        fullWidth: true,
      },
      {
        label: 'HTML 正文',
        dataIndex: 'htmlBody',
        type: 'textarea',
        rules: [{ required: true, max: 200000 }],
        fullWidth: true,
      },
      { label: '纯文本正文', dataIndex: 'textBody', type: 'textarea', fullWidth: true },
    ],
    [accountSelector, bccSelector, ccSelector, recipientSelector],
  );
  const send = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const to = values.toUsers as Array<{ id: string }>;
      const cc = (values.ccUsers ?? []) as Array<{ id: string }>;
      const bcc = (values.bccUsers ?? []) as Array<{ id: string }>;
      const account = values.account as { id: string } | undefined;
      await emailApi.send({
        ...values,
        accountId: account?.id,
        toUserIds: to.map((v) => v.id),
        ccUserIds: cc.map((v) => v.id),
        bccUserIds: bcc.map((v) => v.id),
        toUsers: undefined,
        ccUsers: undefined,
        bccUsers: undefined,
        account: undefined,
      });
      message.success('邮件任务已创建');
    },
  });
  const handleSend = async (values: Record<string, unknown>) => {
    const confirmed = await new Promise<boolean>((resolve) =>
      modal.confirm({
        title: '确认提交正式邮件？',
        content: '提交后内容不可修改，系统将使用持久化队列发送。',
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      }),
    );
    if (!confirmed) return false;
    await send.mutateAsync(values);
    return true;
  };
  return (
    <EditPage
      access={composeAccess}
      title="发送邮件"
      fields={fields}
      initialValues={{ toUsers: [], ccUsers: [], bccUsers: [] }}
      operationType={OperationType.EDIT}
      onSave={handleSend}
      saveLabel="发送"
      saving={send.isPending}
      closeGuard={{ appNumber: props.appNumber, tabKey: props.tabKey }}
    />
  );
};
export default EmailComposePage;
