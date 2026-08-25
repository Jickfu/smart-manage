import request from '@/api/request';
import type { Result } from '@/types/api';
import type { HistoryPoint, MonitorHost, MonitorInstance, RuntimeSnapshot } from './types';

export const runtimeMonitorApi = {
  instances: () =>
    request
      .get<Result<MonitorInstance[]>>('/sys/monitor/runtime/instances')
      .then((r) => r.data.data),
  topology: () =>
    request.get<Result<MonitorHost[]>>('/sys/monitor/runtime/topology').then((r) => r.data.data),
  snapshot: (instanceId?: string) =>
    request
      .get<Result<RuntimeSnapshot>>('/sys/monitor/runtime/snapshot', { params: { instanceId } })
      .then((r) => r.data.data),
  history: (scopeType: 'HOST' | 'INSTANCE', scopeId: string, range: string) =>
    request
      .get<
        Result<HistoryPoint[]>
      >('/sys/monitor/runtime/history', { params: { scopeType, scopeId, range } })
      .then((r) => r.data.data),
};
