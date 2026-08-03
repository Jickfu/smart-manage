import request from '@/api/request';
import type { Result } from '@/types/api';
import type { CacheOverview } from './types';

export const cacheApi = {
  overview: () =>
    request.post<Result<CacheOverview>>('/sys/monitor/cache/overview').then((r) => r.data.data),
  clear: (cacheName: string) =>
    request
      .post<Result<string>>('/sys/monitor/cache/clear', { cacheName })
      .then((r) => r.data.data),
  clearAll: () =>
    request.post<Result<string>>('/sys/monitor/cache/clearAll').then((r) => r.data.data),
};
