export const orgQueryKeys = {
  all: ['sys', 'base', 'org'] as const,
  lists: () => [...orgQueryKeys.all, 'list'] as const,
  list: (params: object) => [...orgQueryKeys.lists(), params] as const,
  details: () => [...orgQueryKeys.all, 'detail'] as const,
  detail: (id: string | null) => [...orgQueryKeys.details(), id] as const,
  tree: (showArchived = false) => [...orgQueryKeys.all, 'tree', showArchived] as const,
};
