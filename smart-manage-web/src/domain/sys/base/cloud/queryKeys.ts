import type { CloudListForm } from './types';

export const cloudQueryKeys = {
  all: ['sys', 'cloud'] as const,
  lists: () => [...cloudQueryKeys.all, 'list'] as const,
  list: (params: Partial<CloudListForm>) => [...cloudQueryKeys.lists(), params] as const,
  details: () => [...cloudQueryKeys.all, 'detail'] as const,
  detail: (id: string | null) => [...cloudQueryKeys.details(), id] as const,
};
