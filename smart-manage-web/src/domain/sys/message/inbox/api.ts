import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  InboxCursorPage,
  InboxDetail,
  InboxMessageDefaults,
  InboxMessageDetail,
  InboxMessageListForm,
  InboxMessageListItem,
  InboxMessageSaveForm,
  InboxReceiptKey,
  InboxUnreadSummary,
} from './types';

export const inboxAdminApi = {
  listPage: (form: InboxMessageListForm) =>
    request
      .post<Result<PageData<InboxMessageListItem>>>('/sys/message/inbox/admin/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<InboxMessageDetail>>('/sys/message/inbox/admin/detail', { id })
      .then((response) => response.data.data),
  createNewData: () =>
    request
      .get<Result<InboxMessageDefaults>>('/sys/message/inbox/admin/createNewData')
      .then((response) => response.data.data),
  save: (form: InboxMessageSaveForm) =>
    request
      .post<Result<string>>('/sys/message/inbox/admin/save', form)
      .then((response) => response.data.data),
  publish: (form: { id: string; version: number }) =>
    request.post<Result<string>>('/sys/message/inbox/admin/publish', form),
  retry: (form: { id: string; version: number }) =>
    request.post<Result<string>>('/sys/message/inbox/admin/retry', form),
};

export const inboxApi = {
  unreadSummary: () =>
    request
      .get<Result<InboxUnreadSummary>>('/sys/message/inbox/unread-summary')
      .then((response) => response.data.data),
  list: (form: {
    pageSize: number;
    unreadOnly: boolean;
    cursorTime?: string;
    cursorMessageId?: string;
  }) =>
    request
      .post<Result<InboxCursorPage>>('/sys/message/inbox/list', form)
      .then((response) => response.data.data),
  detail: (key: InboxReceiptKey) =>
    request
      .post<Result<InboxDetail>>('/sys/message/inbox/detail', key)
      .then((response) => response.data.data),
  markRead: (receipts: InboxReceiptKey[]) =>
    request.post<Result<string>>('/sys/message/inbox/mark-read', { receipts }),
  markUnread: (receipts: InboxReceiptKey[]) =>
    request.post<Result<string>>('/sys/message/inbox/mark-unread', { receipts }),
  markAllRead: () => request.post<Result<string>>('/sys/message/inbox/mark-all-read'),
};
