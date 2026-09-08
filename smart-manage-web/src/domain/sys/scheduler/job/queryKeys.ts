import type { SchedulerScope } from '../common/schedulerScope';

export const jobQueryKeys = {
  all: ['sys', 'scheduler', 'job'] as const,
  list: (status?: string, scope: SchedulerScope = {}) =>
    [...jobQueryKeys.all, 'list', status ?? 'ALL', scope] as const,
  catalog: () => [...jobQueryKeys.all, 'catalog'] as const,
  detail: (id?: string) => [...jobQueryKeys.all, 'detail', id] as const,
  classes: () => [...jobQueryKeys.all, 'classes'] as const,
  cronPreview: (expression: string) => [...jobQueryKeys.all, 'cron-preview', expression] as const,
  createNewData: () => [...jobQueryKeys.all, 'create-new-data'] as const,
};
