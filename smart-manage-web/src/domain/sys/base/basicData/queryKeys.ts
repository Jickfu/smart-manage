import type { BasicDataListForm } from './types';

export const basicDataQueryKeys = {
  all: ['sys', 'basic-data'] as const,
  tree: () => [...basicDataQueryKeys.all, 'tree'] as const,
  categories: () => [...basicDataQueryKeys.all, 'category'] as const,
  category: (id?: string | null) => [...basicDataQueryKeys.categories(), id] as const,
  lists: () => [...basicDataQueryKeys.all, 'list'] as const,
  list: (params: Partial<BasicDataListForm>) => [...basicDataQueryKeys.lists(), params] as const,
  details: () => [...basicDataQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...basicDataQueryKeys.details(), id] as const,
  parentOptions: (categoryId?: string, excludeId?: string) =>
    [...basicDataQueryKeys.all, 'parent-options', categoryId, excludeId] as const,
};
