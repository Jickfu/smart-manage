import type { DomainListForm } from './types';

export const domainQueryKeys = {
  all: ['sys', 'domain'] as const,
  lists: () => [...domainQueryKeys.all, 'list'] as const,
  list: (params: Partial<DomainListForm>) => [...domainQueryKeys.lists(), params] as const,
  details: () => [...domainQueryKeys.all, 'detail'] as const,
  detail: (id: string | null) => [...domainQueryKeys.details(), id] as const,
};
