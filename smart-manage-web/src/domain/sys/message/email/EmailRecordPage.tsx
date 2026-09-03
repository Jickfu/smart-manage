import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { emailApi } from './api';
import { recordAccess } from './permissions';
import type { EmailRecord } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
const labels: Record<string, string> = {
  PENDING: '待发送',
  SENDING: '发送中',
  SUCCESS: 'SMTP 已接受',
  RETRY_WAIT: '等待重试',
  FAILED: '失败',
  UNKNOWN: '结果未知',
  CANCELLED: '已取消',
};
const columnFeatures: ListColumnFeatures = {
  subject: { label: '主题', filter: { type: 'string' }, sorter: true },
  toAddresses: { label: '收件人', filter: { type: 'string' } },
  accountNumber: { label: '发信账号', filter: { type: 'string' } },
  status: {
    label: '状态',
    filter: {
      type: 'enum',
      options: Object.entries(labels).map(([value, label]) => ({ value, label })),
    },
    sorter: true,
  },
  attemptCount: { label: '尝试次数', filter: { type: 'number' } },
  createTime: { label: '创建时间', filter: { type: 'date' }, sorter: true },
};
const EmailRecordPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((s) => s.openBillTab);
  const queryClient = useQueryClient();
  const list = useListPageQuery({
    queryKey: ['email', 'records'],
    queryFn: emailApi.recordList,
  });
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.id === selectedKeys[0])
      : undefined;
  const command = useCommandMutation({
    mutationFn: async (kind: 'retry' | 'cancel') => {
      if (!selected) throw new Error('请先选择记录');
      if (kind === 'retry') await emailApi.retry(selected.id);
      else await emailApi.cancel({ id: selected.id, version: selected.version });
    },
    onSuccess: async (_, kind) => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: ['email', 'records'] });
      feedback.success(kind === 'retry' ? '已创建新的重发任务' : '已取消');
    },
  });
  const columns: ColumnsType<EmailRecord> = [
    {
      title: '主题',
      dataIndex: 'subject',
      render: (value: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(
              props.appNumber,
              'sys/message/email-record/detail',
              record.id,
              OperationType.VIEW,
            )
          }
        >
          {value}
        </Button>
      ),
    },
    {
      title: '收件人',
      dataIndex: 'to',
      width: 260,
      ellipsis: true,
      render: (value: string[]) => value.join('; '),
    },
    { title: '发信账号', dataIndex: 'accountNumber', width: 140 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      render: (value: string) => (
        <Tag
          color={
            value === 'SUCCESS'
              ? 'success'
              : value === 'FAILED' || value === 'UNKNOWN'
                ? 'error'
                : 'default'
          }
        >
          {labels[value] ?? value}
        </Tag>
      ),
    },
    { title: '尝试', dataIndex: 'attemptCount', width: 80 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  return (
    <ListPage<EmailRecord>
      {...props}
      title="发送记录"
      access={recordAccess}
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索主题或收件地址"
      toolbarActions={[
        {
          key: 'retry',
          label: '重新发送',
          permission: recordAccess.permissions.retry,
          disabled: !selected || !['FAILED', 'UNKNOWN', 'CANCELLED'].includes(selected.status),
          loading: command.isPending,
          onClick: () =>
            selected &&
            void confirmOperation({
              type: 'normal',
              title: '确认创建新的重发任务？',
              description: '系统将根据原邮件内容创建一条新的发送任务，是否继续？',
              confirmText: '重新发送',
              onConfirm: () => command.mutateAsync('retry'),
            }),
        },
        {
          key: 'cancel',
          label: '取消',
          permission: recordAccess.permissions.cancel,
          disabled: !selected || !['PENDING', 'RETRY_WAIT'].includes(selected.status),
          loading: command.isPending,
          onClick: () => command.mutate('cancel'),
        },
      ]}
      onRefresh={list.onRefresh}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...list.columnQueryProps}
      dataSource={list.records}
      selectMode="checkbox"
      selectedRowKeys={selectedKeys}
      onSelectChange={setSelectedKeys}
    />
  );
};
export default EmailRecordPage;
