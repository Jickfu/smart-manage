export const monitorAlertQueryKeys = {
  all: ['sys-monitor-alert'] as const,
  rules: () => ['sys-monitor-alert', 'rules'] as const,
  incidents: (page: number, size: number, status?: string) =>
    ['sys-monitor-alert', 'incidents', page, size, status] as const,
};
