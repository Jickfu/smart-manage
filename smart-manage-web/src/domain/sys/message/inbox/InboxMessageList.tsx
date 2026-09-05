import { useMemo, useState } from 'react';
import { Button, Empty, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import { formatInboxTime, inboxReceiptId, inboxLevelLabels } from './inboxPresentation';
import type { InboxCursor, InboxItem, InboxListFilter, InboxReceiptKey } from './types';
import '@/domain/common/page/list/ListPage.css';
import './InboxCenter.css';
import {
  useListColumnFeatures,
  createFilterSummaryLabel,
} from '@/domain/common/page/list/useListColumnFeatures';
import ListFilterSummary from '@/domain/common/page/list/ListFilterSummary';
import { serializeListFilters } from '@/domain/common/page/list/listQuery';
import type { ListColumnFeatures, ListFilterCondition } from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  readStatus: {
    label: '状态',
    filter: {
      type: 'enum',
      options: [
        { label: '未读', value: false },
        { label: '已读', value: true },
      ],
    },
  },
  title: { label: '标题', filter: { type: 'string' } },
  summary: { label: '内容', filter: { type: 'string' } },
  senderName: { label: '发送人', filter: { type: 'string' } },
  receivedTime: { label: '接收时间', filter: { type: 'date' } },
};

export default function InboxMessageList({
  category,
  title,
  active,
  onOpen,
}: {
  category: string;
  title: string;
  active: boolean;
  onOpen: (receipt: InboxReceiptKey, title: string) => void;
}) {
  const [columnFilters, setColumnFilters] = useState<ListFilterCondition[]>([
    { field: 'readStatus', type: 'enum', operator: 'IN', values: [false] },
    { field: 'receivedTime', type: 'date', operator: 'THIS_MONTH' },
  ]);
  const [selectionKeys, setSelectionKeys] = useState<React.Key[]>([]);
  const queryClient = useQueryClient();
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const filter: InboxListFilter = {
    filters: serializeListFilters(columnFilters),
    audienceType:
      category === 'messages-announcement'
        ? 'ALL_ENABLED_USERS'
        : category === 'messages-business'
          ? 'USERS'
          : undefined,
  };
  const listQuery = useInfiniteQuery({
    queryKey: inboxQueryKeys.list(filter),
    queryFn: ({ pageParam }) => inboxApi.list({ ...filter, pageSize: 20, ...pageParam }),
    initialPageParam: undefined as InboxCursor | undefined,
    getNextPageParam: (page) =>
      page.hasMore && page.nextCursorTime && page.nextCursorMessageId
        ? { cursorTime: page.nextCursorTime, cursorMessageId: page.nextCursorMessageId }
        : undefined,
    enabled: active,
    meta: { errorPresentation: 'local-initial' },
  });
  const records = useMemo(
    () => listQuery.data?.pages.flatMap((page) => page.records) ?? [],
    [listQuery.data],
  );
  const error = getBlockingQueryError(listQuery);
  const readMutation = useCommandMutation({
    mutationFn: ({ receipts, read }: { receipts: InboxReceiptKey[]; read: boolean }) =>
      read ? inboxApi.markRead(receipts) : inboxApi.markUnread(receipts),
    onSuccess: async (_, variables) => {
      setSelectionKeys([]);
      await queryClient.invalidateQueries({ queryKey: inboxQueryKeys.all });
      feedback.success(variables.read ? '已标记为已读' : '已标记为未读');
    },
  });
  const allReadMutation = useCommandMutation({
    mutationFn: inboxApi.markAllRead,
    onSuccess: async () => {
      setSelectionKeys([]);
      await queryClient.invalidateQueries({ queryKey: inboxQueryKeys.all });
      feedback.success('全部消息已标记为已读');
    },
  });
  const selectedReceipts = records
    .filter((record) => selectionKeys.includes(inboxReceiptId(record)))
    .map(({ messageId, receivedTime }) => ({ messageId, receivedTime }));
  const columns: ColumnsType<InboxItem> = [
    {
      title: '#',
      key: 'sequence',
      width: 44,
      align: 'center',
      fixed: 'left',
      className: 'sm-list-sequence-column',
      render: (_value: unknown, _record, index) => index + 1,
    },
    {
      title: '状态',
      dataIndex: 'readStatus',
      // 首列表头弹层向表格内部展开，避免覆盖左侧分类栏。
      filterDropdownProps: { placement: 'bottomLeft' },
      width: 80,
      render: (read: boolean) => (read ? '已读' : <Tag color="processing">未读</Tag>),
    },
    {
      title: '标题',
      dataIndex: 'title',
      width: 280,
      ellipsis: true,
      render: (title: string, record) => (
        <button
          type="button"
          className="sm-inbox-title-link sm-inbox-ellipsis"
          title={title}
          onClick={() => onOpen(record, record.title)}
        >
          {title}
        </button>
      ),
    },
    { title: '内容', dataIndex: 'summary', ellipsis: true },
    {
      title: '级别',
      dataIndex: 'level',
      width: 80,
      render: (level: InboxItem['level']) => inboxLevelLabels[level],
    },
    {
      title: '发送人',
      dataIndex: 'senderName',
      width: 110,
      ellipsis: true,
      render: (name?: string) => name ?? '系统通知',
    },
    { title: '接收时间', dataIndex: 'receivedTime', width: 150, render: formatInboxTime },
  ];
  const updateFilters = (filters: ListFilterCondition[]) => {
    setColumnFilters(filters);
    setSelectionKeys([]);
  };
  const configuredColumns = useListColumnFeatures({
    columns,
    features: columnFeatures,
    filters: columnFilters,
    onFiltersChange: updateFilters,
  });
  return (
    <div className="sm-inbox-list-page">
      <div className="sm-list-top">
        <div className="sm-list-filter">
          <div className="sm-list-filter-main sm-inbox-filter-row">
            <h2 className="sm-list-filter-title">{title}</h2>
            {columnFilters.length > 0 && (
              <div className="sm-list-filter-summary">
                <ListFilterSummary
                  items={columnFilters.map((filter) => ({
                    key: filter.field,
                    label:
                      filter.field === 'readStatus'
                        ? `状态：${filter.values?.map((value) => (value ? '已读' : '未读')).join('、')}`
                        : createFilterSummaryLabel(filter, columnFeatures[filter.field]),
                    onRemove: () =>
                      updateFilters(columnFilters.filter((item) => item.field !== filter.field)),
                  }))}
                />
              </div>
            )}
          </div>
        </div>
        <div className="sm-list-toolbar sm-inbox-list-actions">
          <Button
            type="primary"
            disabled={!selectedReceipts.length || selectedReceipts.length > 100 || Boolean(error)}
            loading={readMutation.isPending}
            onClick={() => readMutation.mutate({ receipts: selectedReceipts, read: true })}
          >
            标记已读
          </Button>
          <Button
            type="primary"
            disabled={!selectedReceipts.length || selectedReceipts.length > 100 || Boolean(error)}
            loading={readMutation.isPending}
            onClick={() => readMutation.mutate({ receipts: selectedReceipts, read: false })}
          >
            标记未读
          </Button>
          <Button
            type="primary"
            loading={allReadMutation.isPending}
            disabled={Boolean(error)}
            onClick={() =>
              void confirmOperation({
                type: 'normal',
                title: '全部标记为已读？',
                description: '将近一年全部消息标记为已读，不受当前类型和时间筛选限制。',
                confirmText: '全部已读',
                onConfirm: () => allReadMutation.mutateAsync(),
              })
            }
          >
            全部已读
          </Button>
          <Button
            type="primary"
            loading={listQuery.isFetching}
            onClick={() => {
              setSelectionKeys([]);
              void queryClient.invalidateQueries({ queryKey: inboxQueryKeys.all });
            }}
          >
            刷新
          </Button>
        </div>
      </div>
      <section className="sm-list-table-shell">
        <div className="sm-list-table-meta">
          <span>
            已加载 {records.length} 条
            {selectionKeys.length > 0 ? ` · 已选 ${selectionKeys.length} 条` : ''}
            {selectionKeys.length > 100 ? ' · 单次最多处理100条，请减少选择' : ''}
          </span>
          {listQuery.hasNextPage && !error && (
            <Button
              type="link"
              loading={listQuery.isFetchingNextPage}
              onClick={() => void listQuery.fetchNextPage()}
            >
              加载更多
            </Button>
          )}
        </div>
        {error ? (
          <RequestErrorState error={error} onRetry={() => void listQuery.refetch()} />
        ) : (
          <div className="sm-list-table-body">
            <Table<InboxItem>
              className="sm-list-table"
              size="small"
              rowKey={inboxReceiptId}
              columns={configuredColumns}
              dataSource={records}
              loading={listQuery.isLoading}
              pagination={false}
              tableLayout="fixed"
              scroll={{ x: 1000, y: 1 }}
              rowSelection={{
                columnWidth: 36,
                fixed: true,
                selectedRowKeys: selectionKeys,
                onChange: setSelectionKeys,
              }}
              locale={{
                emptyText: (
                  <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="当前筛选下暂无消息" />
                ),
              }}
            />
          </div>
        )}
      </section>
    </div>
  );
}
