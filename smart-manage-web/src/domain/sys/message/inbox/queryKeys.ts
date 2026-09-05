export const inboxAdminQueryKeys = {
  all: ['sys', 'message', 'inbox-admin'] as const,
  lists: () => [...inboxAdminQueryKeys.all, 'list'] as const,
  list: (params: object) => [...inboxAdminQueryKeys.lists(), params] as const,
  details: () => [...inboxAdminQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...inboxAdminQueryKeys.details(), id] as const,
};

export const inboxQueryKeys = {
  all: ['sys', 'message', 'inbox'] as const,
  unread: () => [...inboxQueryKeys.all, 'unread'] as const,
  lists: () => [...inboxQueryKeys.all, 'list'] as const,
  list: (filter: object) => [...inboxQueryKeys.lists(), filter] as const,
  preview: () => [...inboxQueryKeys.all, 'preview'] as const,
  detail: (messageId?: string, receivedTime?: string) =>
    [...inboxQueryKeys.all, 'detail', messageId, receivedTime] as const,
};
