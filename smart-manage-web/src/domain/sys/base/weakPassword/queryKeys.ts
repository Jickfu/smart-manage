import type { WeakPasswordListForm } from './types';

export const weakPasswordQueryKeys = {
  all: ['sys', 'weak-password'] as const,
  lists: () => [...weakPasswordQueryKeys.all, 'list'] as const,
  list: (params: Partial<WeakPasswordListForm>) =>
    [...weakPasswordQueryKeys.lists(), params] as const,
  details: () => [...weakPasswordQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...weakPasswordQueryKeys.details(), id] as const,
};
