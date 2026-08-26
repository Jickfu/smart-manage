export const runtimeMonitorQueryKeys = {
  all: ['sys-monitor-runtime'] as const,
  instances: () => ['sys-monitor-runtime', 'instances'] as const,
  topology: () => ['sys-monitor-runtime', 'topology'] as const,
  hostSnapshot: (id?: string) => ['sys-monitor-runtime', 'host-snapshot', id] as const,
  instanceSnapshot: (id?: string) => ['sys-monitor-runtime', 'instance-snapshot', id] as const,
  history: (type: string, id: string, range: string) =>
    ['sys-monitor-runtime', 'history', type, id, range] as const,
};
