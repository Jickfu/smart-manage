import { useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { inboxAdminApi } from './api';
import { inboxBroadcastAccess } from './permissions';
import { inboxAdminQueryKeys } from './queryKeys';
import type { InboxLevel, InboxMessageListItem, InboxMessageStatus } from './types';

const statusLabels: Record<InboxMessageStatus, string> = {
  DRAFT: '草稿',
  PENDING: '等待发布',
  PUBLISHING: '发布中',
  PUBLISHED: '已发布',
  FAILED: '发布失败',
};
const statusColors: Record<InboxMessageStatus, string> = {
  DRAFT: 'default',
  PENDING: 'processing',
  PUBLISHING: 'processing',
  PUBLISHED: 'success',
  FAILED: 'error',
};
const levelLabels: Record<InboxLevel, string> = {
  NORMAL: '普通',
  IMPORTANT: '重要',
  URGENT: '紧急',
};

const columnFeatures: ListColumnFeatures = {
  title: { label: '标题', filter: { type: 'string' }, sorter: true },
  level: {
    label: '级别',
    filter: {
      type: 'enum',
      options: Object.entries(levelLabels).map(([value, label]) => ({ value, label })),
    },
    sorter: true,
  },
  status: {
    label: '状态',
    filter: {
      type: 'enum',
      options: Object.entries(statusLabels).map(([value, label]) => ({ value, label })),
    },
    sorter: true,
  },
  senderName: { label: '发布人', filter: { type: 'string' }, sorter: true },
  recipientCount: { label: '收件人数', filter: { type: 'number' }, sorter: true },
  publishTime: { label: '发布时间', filter: { type: 'date' }, sorter: true },
  expireTime: { label: '失效时间', filter: { type: 'date' }, sorter: true },
  createTime: { label: '创建时间', filter: { type: 'date' }, sorter: true },
};

const InboxMessageListPage = (props: PageComponentProps) => {
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const list = useListPageQuery({
    queryKey: inboxAdminQueryKeys.list({}),
    queryFn: inboxAdminApi.listPage,
  });
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.id === selectedKeys[0])
      : undefined;
  const command = useCommandMutation({
    mutationFn: async (record: InboxMessageListItem) => {
      if (record.status === 'FAILED') {
        await inboxAdminApi.retry({ id: record.id, version: record.version });
        return '已提交重试';
      }
      await inboxAdminApi.publish({ id: record.id, version: record.version });
      return '已进入发布队列';
    },
    onSuccess: async (message) => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: inboxAdminQueryKeys.all });
      feedback.success(message);
    },
  });
  const columns: ColumnsType<InboxMessageListItem> = [
    {
      title: '标题',
      dataIndex: 'title',
      ellipsis: true,
      render: (value: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(
              props.appNumber,
              componentKeys.inboxBroadcastEdit,
              record.id,
              record.status === 'DRAFT' ? OperationType.EDIT : OperationType.VIEW,
            )
          }
        >
          {value}
        </Button>
      ),
    },
    {
      title: '级别',
      dataIndex: 'level',
      width: 90,
      render: (value: InboxLevel) => levelLabels[value],
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 110,
      render: (value: InboxMessageStatus) => (
        <Tag color={statusColors[value]}>{statusLabels[value]}</Tag>
      ),
    },
    { title: '发布人', dataIndex: 'senderName', width: 120 },
    { title: '收件人数', dataIndex: 'recipientCount', width: 110 },
    { title: '发布时间', dataIndex: 'publishTime', width: 170, render: (value) => value ?? '—' },
    { title: '失效时间', dataIndex: 'expireTime', width: 170 },
  ];
  const canPublish = selected?.status === 'DRAFT' || selected?.status === 'FAILED';
  return (
    <ListPage<InboxMessageListItem>
      {...props}
      title="消息发布"
      access={inboxBroadcastAccess}
      loading={list.query.isLoading}
      error={list.query.error as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索消息标题/正文"
      onAddNew={() => openAddNewTab(props.appNumber, componentKeys.inboxBroadcastEdit)}
      toolbarActions={[
        {
          key: 'publish',
          label: selected?.status === 'FAILED' ? '重试发布' : '发布',
          permission:
            selected?.status === 'FAILED'
              ? inboxBroadcastAccess.permissions.retry
              : inboxBroadcastAccess.permissions.publish,
          disabled: !canPublish,
          loading: command.isPending,
          onClick: () => {
            if (!selected || !canPublish) return;
            void confirmOperation({
              type: 'warning',
              title: selected.status === 'FAILED' ? '确认重试发布？' : '确认发布全站消息？',
              description:
                selected.status === 'FAILED'
                  ? selected.title
                  : `“${selected.title}”将发送给发布时所有启用用户，发布后不能修改。`,
              confirmText: selected.status === 'FAILED' ? '重试发布' : '确认发布',
              onConfirm: () => command.mutateAsync(selected),
            });
          },
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

export default InboxMessageListPage;
