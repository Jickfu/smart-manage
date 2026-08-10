import request from '@/api/request';
import type { Result } from '@/types/api';
import { nodeMonitorApi } from '../node/api';
import type { SlowSqlSnapshot } from './types';

export const slowSqlApi = {
  instances: nodeMonitorApi.instances,
  snapshot: (instanceId?: string) =>
    request
      .get<Result<SlowSqlSnapshot>>('/sys/monitor/slow-sql/snapshot', {
        params: { instanceId },
      })
      .then((response) => response.data.data),
  updateThreshold: (instanceId: string, thresholdMs: number) =>
    request
      .post<Result<SlowSqlSnapshot>>('/sys/monitor/slow-sql/threshold', {
        instanceId,
        thresholdMs,
      })
      .then((response) => response.data.data),
  clear: (instanceId: string) =>
    request
      .post<Result<SlowSqlSnapshot>>('/sys/monitor/slow-sql/clear', { instanceId })
      .then((response) => response.data.data),
};
