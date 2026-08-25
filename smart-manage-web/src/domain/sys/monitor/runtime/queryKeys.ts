export const runtimeMonitorQueryKeys = {
  all: ['sys-monitor-runtime'] as const,
  instances: () => ['sys-monitor-runtime', 'instances'] as const,
  topology: () => ['sys-monitor-runtime', 'topology'] as const,
  snapshot: (id?: string) => ['sys-monitor-runtime', 'snapshot', id] as const,
  history: (type: string, id: string, range: string) =>
    ['sys-monitor-runtime', 'history', type, id, range] as const,
};
