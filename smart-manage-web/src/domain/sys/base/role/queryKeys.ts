export const roleQueryKeys = {
  all: ['sys', 'role'] as const,
  lists: () => [...roleQueryKeys.all, 'list'] as const,
  list: (params: object) => [...roleQueryKeys.lists(), params] as const,
  listAll: () => [...roleQueryKeys.all, 'list-all'] as const,
  details: () => [...roleQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...roleQueryKeys.details(), id] as const,
};
