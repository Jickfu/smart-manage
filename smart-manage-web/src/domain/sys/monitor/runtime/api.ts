import request from '@/api/request';
import type { Result } from '@/types/api';
import type {
  HistoryPoint,
  HostSnapshot,
  InstanceSnapshot,
  MonitorHost,
  MonitorInstance,
} from './types';

export const runtimeMonitorApi = {
  instances: () =>
    request
      .get<Result<MonitorInstance[]>>('/sys/monitor/runtime/instances')
      .then((r) => r.data.data),
  topology: () =>
    request.get<Result<MonitorHost[]>>('/sys/monitor/runtime/topology').then((r) => r.data.data),
  hostSnapshot: (hostId: string) =>
    request
      .get<Result<HostSnapshot>>('/sys/monitor/runtime/host-snapshot', { params: { hostId } })
      .then((r) => r.data.data),
  instanceSnapshot: (instanceId?: string) =>
    request
      .get<
        Result<InstanceSnapshot>
      >('/sys/monitor/runtime/instance-snapshot', { params: { instanceId } })
      .then((r) => r.data.data),
  retire: (instanceId: string) =>
    request.post<Result<void>>('/sys/monitor/runtime/instances/retire', undefined, {
      params: { instanceId },
    }),
  history: (scopeType: 'HOST' | 'INSTANCE', scopeId: string, range: string) =>
    request
      .get<
        Result<HistoryPoint[]>
      >('/sys/monitor/runtime/history', { params: { scopeType, scopeId, range } })
      .then((r) => r.data.data),
};
