import type { AppListForm } from './types';

export const appQueryKeys = {
  all: ['sys', 'app'] as const,
  lists: () => [...appQueryKeys.all, 'list'] as const,
  list: (params: Partial<AppListForm>) => [...appQueryKeys.lists(), params] as const,
  details: () => [...appQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...appQueryKeys.details(), id] as const,
  domainApps: () => [...appQueryKeys.all, 'domain-apps'] as const,
  domainAppsAll: () => [...appQueryKeys.all, 'domain-apps-all'] as const,
};
