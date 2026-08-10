export const nodeMonitorQueryKeys = {
  all: ['sys', 'monitor', 'node'] as const,
  instances: () => ['sys', 'monitor', 'instances'] as const,
  snapshot: (instanceId?: string) => ['sys', 'monitor', 'node', 'snapshot', instanceId] as const,
};
