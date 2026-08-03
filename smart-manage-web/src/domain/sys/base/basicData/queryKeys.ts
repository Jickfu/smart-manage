import type { BasicDataListForm } from './types';

export const basicDataQueryKeys = {
  all: ['sys', 'basic-data'] as const,
  lists: () => [...basicDataQueryKeys.all, 'list'] as const,
  list: (params: Partial<BasicDataListForm>) => [...basicDataQueryKeys.lists(), params] as const,
  details: () => [...basicDataQueryKeys.all, 'detail'] as const,
  detail: (id: string | undefined) => [...basicDataQueryKeys.details(), id] as const,
  createNewData: (tabKey: string) =>
    [...basicDataQueryKeys.all, 'create-new-data', tabKey] as const,
};
