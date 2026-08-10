export const slowSqlQueryKeys = {
  all: ['sys', 'monitor', 'slow-sql'] as const,
  instances: () => ['sys', 'monitor', 'instances'] as const,
  snapshot: (instanceId?: string) =>
    ['sys', 'monitor', 'slow-sql', 'snapshot', instanceId] as const,
};
