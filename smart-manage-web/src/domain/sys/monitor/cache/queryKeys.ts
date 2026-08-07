import type { CacheScopeFilter } from './types';

export const cacheQueryKeys = {
  all: ['sys-monitor-cache'] as const,
  overview: () => ['sys-monitor-cache', 'overview'] as const,
  runtime: () => ['sys-monitor-cache', 'runtime'] as const,
  scopeTree: () => ['sys-monitor-cache', 'scope-tree'] as const,
  entries: (scope: CacheScopeFilter) => ['sys-monitor-cache', 'entries', scope] as const,
  value: (id?: string) => ['sys-monitor-cache', 'value', id] as const,
};
