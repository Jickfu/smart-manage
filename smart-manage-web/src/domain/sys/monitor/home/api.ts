import request from '@/api/request';
import type { Result } from '@/types/api';
import type { MonitorOverview } from './types';
export const monitorOverviewApi = () =>
  request
    .get<Result<MonitorOverview>>('/sys/monitor/overview')
    .then((response) => response.data.data);
