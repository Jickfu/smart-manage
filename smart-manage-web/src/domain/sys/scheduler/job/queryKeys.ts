export const jobQueryKeys = {
  all: ['sys', 'scheduler', 'job'] as const,
  list: (status?: string) => [...jobQueryKeys.all, 'list', status ?? 'ALL'] as const,
  detail: (id?: string) => [...jobQueryKeys.all, 'detail', id] as const,
  classes: () => [...jobQueryKeys.all, 'classes'] as const,
  cronPreview: (expression: string) => [...jobQueryKeys.all, 'cron-preview', expression] as const,
  createNewData: () => [...jobQueryKeys.all, 'create-new-data'] as const,
};
