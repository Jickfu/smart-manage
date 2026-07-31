import request from '@/api/request';
import type { Result } from '@/types/api';
import type { SchedulerSummary } from './types';

export const schedulerHomeApi = {
  summary: () =>
    request
      .get<Result<SchedulerSummary>>('/sys/scheduler/home/summary')
      .then((response) => response.data.data),
};
