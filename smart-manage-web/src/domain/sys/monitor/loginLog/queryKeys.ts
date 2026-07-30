export const loginLogQueryKeys = {
  all: ['sys', 'monitor', 'login-log'] as const,
  lists: () => [...loginLogQueryKeys.all, 'list'] as const,
  list: (params: object) => [...loginLogQueryKeys.lists(), params] as const,
  details: () => [...loginLogQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...loginLogQueryKeys.details(), id] as const,
};
