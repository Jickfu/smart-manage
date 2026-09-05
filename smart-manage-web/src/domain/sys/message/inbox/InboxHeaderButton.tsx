import { useState } from 'react';
import { Badge, Button } from 'antd';
import { MessageOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import InboxPreviewDrawer from './InboxPreviewDrawer';
import { inboxPollingInterval } from './inboxPresentation';

export default function InboxHeaderButton() {
  const [open, setOpen] = useState(false);
  const unreadQuery = useQuery({
    queryKey: inboxQueryKeys.unread(),
    queryFn: inboxApi.unreadSummary,
    refetchInterval: (query) => inboxPollingInterval(query.state.data?.pollingIntervalSeconds),
    refetchIntervalInBackground: false,
  });
  const summary = unreadQuery.data;
  return (
    <>
      <Badge count={summary?.overflow ? '99+' : summary?.unreadCount} size="small" offset={[-4, 4]}>
        <Button
          className="sm-header-action-button"
          type="text"
          icon={<MessageOutlined />}
          aria-label="消息通知"
          aria-expanded={open}
          onClick={() => {
            if (!open) void unreadQuery.refetch();
            setOpen(!open);
          }}
        />
      </Badge>
      <InboxPreviewDrawer open={open} onClose={() => setOpen(false)} />
    </>
  );
}
