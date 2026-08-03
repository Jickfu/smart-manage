import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { RedisValue } from '../redis/types';
import type { CacheEntry, CacheEntryKey, CacheOverview } from './types';

export const cacheApi = {
  overview: () =>
    request.post<Result<CacheOverview>>('/sys/monitor/cache/overview').then((r) => r.data.data),
  clear: (cacheName: string) =>
    request
      .post<Result<string>>('/sys/monitor/cache/clear', { cacheName })
      .then((r) => r.data.data),
  clearAll: () =>
    request.post<Result<string>>('/sys/monitor/cache/clearAll').then((r) => r.data.data),
  listPage: (form: {
    pageNum: number;
    pageSize: number;
    keyword?: string;
    storage?: string;
    cacheName?: string;
  }) =>
    request
      .post<Result<PageData<CacheEntry>>>('/sys/monitor/cache/listPage', form)
      .then((r) => r.data.data),
  value: (entry: CacheEntryKey) =>
    request.post<Result<RedisValue>>('/sys/monitor/cache/value', entry).then((r) => r.data.data),
  delete: (entries: CacheEntryKey[]) =>
    request.post<Result<number>>('/sys/monitor/cache/delete', { entries }).then((r) => r.data.data),
};
