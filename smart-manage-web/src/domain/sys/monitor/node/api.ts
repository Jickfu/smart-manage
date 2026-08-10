import request from '@/api/request';
import type { Result } from '@/types/api';
import type { MonitorInstance, NodeSnapshot } from './types';

export const nodeMonitorApi = {
  instances: () =>
    request
      .get<Result<MonitorInstance[]>>('/sys/monitor/instances')
      .then((response) => response.data.data),
  snapshot: (instanceId?: string) =>
    request
      .get<Result<NodeSnapshot>>('/sys/monitor/node/snapshot', { params: { instanceId } })
      .then((response) => response.data.data),
};
