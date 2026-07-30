import type { AppListForm } from './types';

export const appQueryKeys = {
  all: ['sys', 'app'] as const,
  lists: () => [...appQueryKeys.all, 'list'] as const,
  list: (params: Partial<AppListForm>) => [...appQueryKeys.lists(), params] as const,
  details: () => [...appQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...appQueryKeys.details(), id] as const,
  cloudApps: () => [...appQueryKeys.all, 'cloud-apps'] as const,
  cloudAppsAll: () => [...appQueryKeys.all, 'cloud-apps-all'] as const,
};
