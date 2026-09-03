import { useMemo, useState } from 'react';
import { Button, Empty, List, Segmented, Spin, Tag, Typography } from 'antd';
import { useInfiniteQuery, useQuery, useQueryClient } from '@tanstack/react-query';
import dayjs from 'dayjs';
import AppModal from '@/domain/common/component/AppModal';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import type { InboxItem, InboxLevel, InboxReceiptKey } from './types';
import './InboxCenterModal.css';

interface Cursor {
  cursorTime: string;
  cursorMessageId: string;
}

const levelLabel: Record<InboxLevel, string> = {
  NORMAL: '普通',
  IMPORTANT: '重要',
  URGENT: '紧急',
};

/** receivedTime 同时是微秒精度收件键；展示时只截取时间主体，不改变原键。 */
const formatReceivedTime = (value: string) => dayjs(value.slice(0, 19)).format('YYYY-MM-DD HH:mm');

export default function InboxCenterModal({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [selected, setSelected] = useState<InboxReceiptKey>();
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const listQuery = useInfiniteQuery({
    queryKey: inboxQueryKeys.list(unreadOnly),
    queryFn: ({ pageParam }) =>
      inboxApi.list({ pageSize: 20, unreadOnly, ...(pageParam as Cursor | undefined) }),
    initialPageParam: undefined as Cursor | undefined,
    getNextPageParam: (page) =>
      page.hasMore && page.nextCursorTime && page.nextCursorMessageId
        ? { cursorTime: page.nextCursorTime, cursorMessageId: page.nextCursorMessageId }
        : undefined,
    enabled: open,
  });
  const records = useMemo(
    () => listQuery.data?.pages.flatMap((page) => page.records) ?? [],
    [listQuery.data],
  );
  const detailQuery = useQuery({
    queryKey: inboxQueryKeys.detail(selected?.messageId, selected?.receivedTime),
    queryFn: () => inboxApi.detail(selected!),
    enabled: open && Boolean(selected),
  });
  const refreshInbox = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: inboxQueryKeys.unread() }),
      queryClient.invalidateQueries({ queryKey: [...inboxQueryKeys.all, 'list'] }),
    ]);
  };
  const readMutation = useCommandMutation({
    mutationFn: ({ key, read }: { key: InboxReceiptKey; read: boolean }) =>
      read ? inboxApi.markRead([key]) : inboxApi.markUnread([key]),
    onSuccess: async (_, variables) => {
      await refreshInbox();
      feedback.success(variables.read ? '已标记为已读' : '已标记为未读');
    },
  });
  const markAllMutation = useCommandMutation({
    mutationFn: inboxApi.markAllRead,
    onSuccess: async () => {
      await refreshInbox();
      feedback.success('全部消息已标记为已读');
    },
  });
  const selectMessage = (record: InboxItem) => {
    const key = { messageId: record.messageId, receivedTime: record.receivedTime };
    setSelected(key);
    if (!record.readStatus) readMutation.mutate({ key, read: true });
  };
  return (
    <AppModal
      title="消息中心"
      open={open}
      width={920}
      className="sm-inbox-center"
      bodyMode="fixed"
      onCancel={onClose}
      footer={<Button onClick={onClose}>关闭</Button>}
      headerExtra={
        <div className="sm-inbox-center-actions">
          <Segmented
            size="small"
            value={unreadOnly ? 'unread' : 'all'}
            options={[
              { label: '全部', value: 'all' },
              { label: '未读', value: 'unread' },
            ]}
            onChange={(value) => {
              setUnreadOnly(value === 'unread');
              setSelected(undefined);
            }}
          />
          <Button
            size="small"
            loading={markAllMutation.isPending}
            onClick={() => markAllMutation.mutate()}
          >
            全部已读
          </Button>
        </div>
      }
    >
      <div className="sm-inbox-center-layout">
        <div className="sm-inbox-center-list">
          <Spin spinning={listQuery.isLoading}>
            <List
              dataSource={records}
              locale={{
                emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无消息" />,
              }}
              renderItem={(record) => (
                <List.Item
                  className={`sm-inbox-center-item ${record.readStatus ? '' : 'sm-inbox-center-item--unread'} ${selected?.messageId === record.messageId && selected.receivedTime === record.receivedTime ? 'sm-inbox-center-item--selected' : ''}`}
                  onClick={() => selectMessage(record)}
                >
                  <div className="sm-inbox-center-item-content">
                    <div className="sm-inbox-center-item-title">
                      {!record.readStatus && <span className="sm-inbox-center-unread-dot" />}
                      <span>{record.title}</span>
                    </div>
                    <Typography.Text type="secondary" ellipsis>
                      {record.summary}
                    </Typography.Text>
                    <span className="sm-inbox-center-item-time">
                      {formatReceivedTime(record.receivedTime)}
                    </span>
                  </div>
                </List.Item>
              )}
            />
          </Spin>
          {listQuery.hasNextPage && (
            <Button
              type="link"
              loading={listQuery.isFetchingNextPage}
              onClick={() => void listQuery.fetchNextPage()}
            >
              加载更多
            </Button>
          )}
        </div>
        <div className="sm-inbox-center-detail">
          {!selected ? (
            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="请选择一条消息" />
          ) : (
            <Spin spinning={detailQuery.isLoading}>
              {detailQuery.data && (
                <article>
                  <div className="sm-inbox-center-detail-heading">
                    <h3>{detailQuery.data.title}</h3>
                    <Tag
                      color={
                        detailQuery.data.level === 'URGENT'
                          ? 'error'
                          : detailQuery.data.level === 'IMPORTANT'
                            ? 'warning'
                            : 'default'
                      }
                    >
                      {levelLabel[detailQuery.data.level]}
                    </Tag>
                  </div>
                  <div className="sm-inbox-center-detail-meta">
                    {detailQuery.data.senderName ?? '系统通知'} ·{' '}
                    {formatReceivedTime(detailQuery.data.receivedTime)}
                  </div>
                  <div className="sm-inbox-center-detail-content">{detailQuery.data.content}</div>
                  <Button
                    size="small"
                    loading={readMutation.isPending}
                    onClick={() => readMutation.mutate({ key: selected, read: false })}
                  >
                    标记为未读
                  </Button>
                </article>
              )}
            </Spin>
          )}
        </div>
      </div>
    </AppModal>
  );
}
