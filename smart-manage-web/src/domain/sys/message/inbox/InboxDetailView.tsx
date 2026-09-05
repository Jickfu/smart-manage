import { useEffect, useRef } from 'react';
import { Button, Tag } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import { formatInboxTime, inboxLevelLabels, inboxReceiptId } from './inboxPresentation';
import type { InboxReceiptKey } from './types';

export default function InboxDetailView({
  receipt,
  active,
  onBack,
  tabKey,
  onTitleChange,
}: {
  receipt: InboxReceiptKey;
  active: boolean;
  onBack: () => void;
  tabKey?: string;
  onTitleChange?: (key: string, title: string) => void;
}) {
  const queryClient = useQueryClient();
  const autoReadKey = useRef<string | undefined>(undefined);
  const detailQuery = useQuery({
    queryKey: inboxQueryKeys.detail(receipt.messageId, receipt.receivedTime),
    queryFn: () => inboxApi.detail(receipt),
    enabled: active,
    meta: { errorPresentation: 'local-initial' },
  });
  const error = getBlockingQueryError(detailQuery);
  const detail = detailQuery.data;
  useEffect(() => {
    if (tabKey && detail && !error) onTitleChange?.(tabKey, detail.title);
  }, [tabKey, detail, error, onTitleChange]);
  const { mutate: markRead, isPending } = useCommandMutation({
    mutationFn: (read: boolean) =>
      read ? inboxApi.markRead([receipt]) : inboxApi.markUnread([receipt]),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: inboxQueryKeys.all });
    },
  });
  useEffect(() => {
    // 只有成功读取自己的详情后才标记；严格模式重放和缓存刷新不重复自动执行。
    const key = inboxReceiptId(receipt);
    if (
      active &&
      detailQuery.isSuccess &&
      !detailQuery.isFetching &&
      detail &&
      !error &&
      !detail.readStatus &&
      autoReadKey.current !== key
    ) {
      autoReadKey.current = key;
      markRead(true);
    }
  }, [active, detailQuery.isSuccess, detailQuery.isFetching, detail, error, markRead, receipt]);
  return (
    <div className="sm-inbox-detail-page">
      <EditPageShell
        title={detail?.title ?? '消息详情'}
        loading={detailQuery.isLoading}
        error={error}
        onRetry={() => {
          if (active) void detailQuery.refetch();
        }}
        actions={
          <>
            <Button type="primary" onClick={onBack}>
              返回列表
            </Button>
            <Button
              type="primary"
              disabled={!detail || Boolean(error)}
              loading={isPending}
              onClick={() => {
                if (active && detail && !error) markRead(!detail.readStatus);
              }}
            >
              {detail?.readStatus ? '标记未读' : '标记已读'}
            </Button>
          </>
        }
      >
        {detail && (
          <article className="sm-inbox-detail">
            <h2>{detail.title}</h2>
            <div className="sm-inbox-detail-meta">
              <Tag
                color={
                  detail.level === 'URGENT'
                    ? 'error'
                    : detail.level === 'IMPORTANT'
                      ? 'warning'
                      : 'default'
                }
              >
                {inboxLevelLabels[detail.level]}
              </Tag>
              <span>{detail.senderName ?? '系统通知'}</span>
              <time>{formatInboxTime(detail.receivedTime)}</time>
            </div>
            <div className="sm-inbox-detail-content">{detail.content}</div>
          </article>
        )}
      </EditPageShell>
    </div>
  );
}
