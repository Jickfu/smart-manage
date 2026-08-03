export const redisQueryKeys = {
  all: ['sys-monitor-redis'] as const,
  runtime: () => ['sys-monitor-redis', 'runtime'] as const,
  keys: (cursor: string, pattern: string) =>
    ['sys-monitor-redis', 'keys', cursor, pattern] as const,
  value: (key?: string) => ['sys-monitor-redis', 'value', key] as const,
};
