export const executionQueryKeys = {
  all: ['sys', 'scheduler', 'execution'] as const,
  list: (status?: string) => [...executionQueryKeys.all, 'list', status ?? 'ALL'] as const,
  detail: (id?: string) => [...executionQueryKeys.all, 'detail', id] as const,
};
