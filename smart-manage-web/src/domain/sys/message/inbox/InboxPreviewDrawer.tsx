import { useState } from 'react';
import { Button, Drawer, Empty, Spin, Tabs } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { openInboxCenter } from '@/services/navigationService';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import { formatInboxTime, inboxReceiptId } from './inboxPresentation';
import type { InboxReceiptKey } from './types';
import './InboxCenter.css';

export default function InboxPreviewDrawer({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const [section, setSection] = useState<'messages' | 'tasks'>('messages');
  const previewQuery = useQuery({
    queryKey: inboxQueryKeys.preview(),
    queryFn: () => inboxApi.list({ pageSize: 10, unreadOnly: true, monthOnly: false }),
    enabled: open && section === 'messages',
    meta: { errorPresentation: 'local-initial' },
  });
  const error = getBlockingQueryError(previewQuery);
  const navigate = (receipt?: InboxReceiptKey) => {
    onClose();
    openInboxCenter(section, receipt);
  };
  return (
    <Drawer
      rootClassName="sm-inbox-preview"
      aria-label="消息与任务预览"
      classNames={{ body: 'sm-inbox-preview-body' }}
      placement="right"
      size="min(400px, 100vw)"
      open={open}
      mask={false}
      focusable={{ trap: false, focusTriggerAfterClose: false }}
      getContainer={false}
      closable={false}
      onClose={onClose}
    >
      <div className="sm-inbox-preview-top">
        <Tabs
          activeKey={section}
          items={[
            { key: 'tasks', label: '任务' },
            { key: 'messages', label: '消息' },
          ]}
          onChange={(key) => setSection(key as 'messages' | 'tasks')}
        />
        <Button type="link" onClick={() => navigate()}>
          更多
        </Button>
        <Button type="text" aria-label="关闭消息预览" onClick={onClose}>
          ×
        </Button>
      </div>
      <div className="sm-inbox-preview-content">
        {section === 'tasks' ? (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="工作流任务暂未开放">
            <Button type="link" onClick={() => navigate()}>
              查看任务中心
            </Button>
          </Empty>
        ) : error ? (
          <RequestErrorState error={error} onRetry={() => void previewQuery.refetch()} />
        ) : (
          <Spin spinning={previewQuery.isLoading}>
            {previewQuery.data?.records.length === 0 && (
              <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无未读消息" />
            )}
            {previewQuery.data?.records.map((record) => (
              <button
                key={inboxReceiptId(record)}
                type="button"
                className="sm-inbox-preview-item"
                onClick={() =>
                  navigate({ messageId: record.messageId, receivedTime: record.receivedTime })
                }
              >
                <span className="sm-inbox-preview-title">
                  {!record.readStatus && <span className="sm-inbox-unread-dot" />}
                  <span className="sm-inbox-ellipsis" title={record.title}>
                    {record.title}
                  </span>
                </span>
                <span className="sm-inbox-preview-summary">{record.summary}</span>
                <time className="sm-inbox-preview-time">
                  {formatInboxTime(record.receivedTime)}
                </time>
              </button>
            ))}
          </Spin>
        )}
      </div>
    </Drawer>
  );
}
