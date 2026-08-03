export const cacheQueryKeys = {
  all: ['sys-monitor-cache'] as const,
  overview: () => ['sys-monitor-cache', 'overview'] as const,
  runtime: () => ['sys-monitor-cache', 'runtime'] as const,
  entries: (storage?: string, cacheName?: string) =>
    ['sys-monitor-cache', 'entries', storage, cacheName] as const,
  value: (id?: string) => ['sys-monitor-cache', 'value', id] as const,
};
