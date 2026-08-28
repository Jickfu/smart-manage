import type { PageForm } from '@/types/api';

export type InboxLevel = 'NORMAL' | 'IMPORTANT' | 'URGENT';
export type InboxMessageStatus = 'DRAFT' | 'PENDING' | 'PUBLISHING' | 'PUBLISHED' | 'FAILED';

export interface InboxMessageListForm extends PageForm {
  keyword?: string;
  status?: InboxMessageStatus;
  level?: InboxLevel;
}

export interface InboxMessageListItem {
  id: string;
  version: number;
  title: string;
  level: InboxLevel;
  status: InboxMessageStatus;
  senderName?: string;
  recipientCount: number;
  publishTime?: string;
  expireTime: string;
  createTime: string;
  errorMessage?: string;
}

export interface InboxMessageDetail extends InboxMessageListItem {
  content: string;
}

export interface InboxMessageDefaults {
  level: InboxLevel;
  expireTime: string;
}

export interface InboxMessageSaveForm {
  id?: string;
  version?: number;
  title: string;
  content: string;
  level: InboxLevel;
  expireTime: string;
}

export interface InboxReceiptKey {
  messageId: string;
  receivedTime: string;
}

export interface InboxItem extends InboxReceiptKey {
  title: string;
  summary: string;
  level: InboxLevel;
  senderName?: string;
  readStatus: boolean;
  readTime?: string;
  expireTime: string;
}

export interface InboxDetail extends InboxItem {
  content: string;
  resourceType?: string;
  resourceId?: string;
  actionCode?: string;
  actionPayload?: string;
}

export interface InboxCursorPage {
  records: InboxItem[];
  hasMore: boolean;
  nextCursorTime?: string;
  nextCursorMessageId?: string;
}

export interface InboxUnreadSummary {
  unreadCount: number;
  overflow: boolean;
}
