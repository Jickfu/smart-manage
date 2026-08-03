export const cacheQueryKeys = {
  all: ['sys-monitor-cache'] as const,
  overview: () => ['sys-monitor-cache', 'overview'] as const,
};
