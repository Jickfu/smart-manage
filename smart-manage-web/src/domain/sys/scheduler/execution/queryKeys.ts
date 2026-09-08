import type { SchedulerScope } from '../common/schedulerScope';

export const executionQueryKeys = {
  all: ['sys', 'scheduler', 'execution'] as const,
  list: (status?: string, scope: SchedulerScope = {}) =>
    [...executionQueryKeys.all, 'list', status ?? 'ALL', scope] as const,
  catalog: () => [...executionQueryKeys.all, 'catalog'] as const,
  detail: (id?: string) => [...executionQueryKeys.all, 'detail', id] as const,
};
