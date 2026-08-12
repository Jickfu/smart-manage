export const featureQueryKeys = {
  all: ['sys', 'feature'] as const,
  lists: () => [...featureQueryKeys.all, 'list'] as const,
  list: (params: object) => [...featureQueryKeys.lists(), params] as const,
  details: () => [...featureQueryKeys.all, 'detail'] as const,
  detail: (id: string | null) => [...featureQueryKeys.details(), id] as const,
  visible: () => [...featureQueryKeys.all, 'visible'] as const,
};
