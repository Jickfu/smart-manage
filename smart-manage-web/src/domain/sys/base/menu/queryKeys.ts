export const menuQueryKeys = {
  all: ['sys', 'menu'] as const,
  lists: () => [...menuQueryKeys.all, 'list'] as const,
  list: (params: object) => [...menuQueryKeys.lists(), params] as const,
  details: () => [...menuQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...menuQueryKeys.details(), id] as const,
  trees: () => [...menuQueryKeys.all, 'tree'] as const,
  userByApp: (appNumber: string) => [...menuQueryKeys.all, 'user-app', appNumber] as const,
};
