export const operateLogQueryKeys = {
  all: ['sys', 'monitor', 'operate-log'] as const,
  lists: () => [...operateLogQueryKeys.all, 'list'] as const,
  list: (params: object) => [...operateLogQueryKeys.lists(), params] as const,
  details: () => [...operateLogQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...operateLogQueryKeys.details(), id] as const,
};
