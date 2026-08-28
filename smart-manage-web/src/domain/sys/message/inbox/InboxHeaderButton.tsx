import { useState } from 'react';
import { Badge, Button } from 'antd';
import { MessageOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import InboxCenterModal from './InboxCenterModal';

export default function InboxHeaderButton() {
  const [open, setOpen] = useState(false);
  const unreadQuery = useQuery({
    queryKey: inboxQueryKeys.unread(),
    queryFn: inboxApi.unreadSummary,
    refetchInterval: 30_000,
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
          aria-label="打开消息中心"
          onClick={() => setOpen(true)}
        />
      </Badge>
      <InboxCenterModal open={open} onClose={() => setOpen(false)} />
    </>
  );
}
