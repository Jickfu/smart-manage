import { Collapse, Descriptions, Table, Tag } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { emailApi } from './api';
import './EmailPage.css';
const EmailRecordDetailPage = ({ billId }: PageComponentProps) => {
  const query = useQuery({
    queryKey: ['email', 'record', billId],
    queryFn: () => emailApi.recordDetail(billId!),
    enabled: !!billId,
  });
  const data = query.data;
  const info = (
    <Descriptions
      column={2}
      items={[
        { key: 'status', label: '状态', children: <Tag>{data?.status}</Tag> },
        { key: 'account', label: '发信账号', children: data?.accountNumber },
        { key: 'from', label: '发件地址', children: data?.fromAddress },
        { key: 'to', label: '收件人', children: data?.to.join('; ') },
        { key: 'cc', label: '抄送', children: data?.cc?.join('; ') || '-' },
        { key: 'bcc', label: '密送', children: data?.bcc?.join('; ') || '-' },
        { key: 'subject', label: '主题', span: 2, children: data?.subject },
        { key: 'error', label: '失败原因', span: 2, children: data?.errorMessage || '-' },
        { key: 'time', label: '创建时间', children: data?.createTime },
        { key: 'completed', label: '完成时间', children: data?.completedTime || '-' },
      ]}
    />
  );
  return (
    <EditPageShell
      title="邮件发送详情"
      actions={null}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
    >
      <Collapse
        className="sm-edit-collapse"
        defaultActiveKey={['base', 'content', 'attempts']}
        items={[
          { key: 'base', label: '投递信息', children: info },
          {
            key: 'content',
            label: '正文快照',
            children: (
              <>
                <h4>HTML 正文源码</h4>
                <div className="sm-email-record-body">{data?.htmlBody}</div>
                <h4>纯文本正文</h4>
                <div className="sm-email-record-body">{data?.textBody || '-'}</div>
              </>
            ),
          },
          {
            key: 'attempts',
            label: '发送尝试',
            children: (
              <Table
                className="sm-email-attempts"
                rowKey={(record) => String(record.id)}
                pagination={false}
                dataSource={data?.attempts ?? []}
                columns={[
                  { title: '次数', dataIndex: 'attemptNo', width: 80 },
                  { title: '状态', dataIndex: 'status', width: 100 },
                  { title: '实例', dataIndex: 'instanceId', width: 160 },
                  { title: '开始时间', dataIndex: 'startedTime', width: 180 },
                  { title: '完成时间', dataIndex: 'completedTime', width: 180 },
                  { title: '错误', dataIndex: 'errorMessage' },
                ]}
              />
            ),
          },
        ]}
      />
    </EditPageShell>
  );
};
export default EmailRecordDetailPage;
