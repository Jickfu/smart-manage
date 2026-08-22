export const emailAccountQueryKeys = {
  all: ['sys', 'message', 'email-account'] as const,
  lists: () => [...emailAccountQueryKeys.all, 'list'] as const,
  list: (params: object) => [...emailAccountQueryKeys.lists(), params] as const,
  details: () => [...emailAccountQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...emailAccountQueryKeys.details(), id] as const,
};
export const emailRecordQueryKeys = {
  all: ['sys', 'message', 'email-record'] as const,
  lists: () => [...emailRecordQueryKeys.all, 'list'] as const,
  list: (params: object) => [...emailRecordQueryKeys.lists(), params] as const,
  details: () => [...emailRecordQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...emailRecordQueryKeys.details(), id] as const,
};
