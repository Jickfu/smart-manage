export const permissionQueryKeys = {
  all: ['sys', 'permission'] as const,
  lists: () => [...permissionQueryKeys.all, 'list'] as const,
  list: (params: object) => [...permissionQueryKeys.lists(), params] as const,
  listAll: () => [...permissionQueryKeys.all, 'list-all'] as const,
  details: () => [...permissionQueryKeys.all, 'detail'] as const,
  detail: (id: string | null) => [...permissionQueryKeys.details(), id] as const,
};
