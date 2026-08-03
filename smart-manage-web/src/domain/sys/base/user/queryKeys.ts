export const userQueryKeys = {
  all: ['sys', 'user'] as const,
  lists: () => [...userQueryKeys.all, 'list'] as const,
  list: (params: object) => [...userQueryKeys.lists(), params] as const,
  details: () => [...userQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...userQueryKeys.details(), id] as const,
  permissions: () => [...userQueryKeys.all, 'permissions'] as const,
};
