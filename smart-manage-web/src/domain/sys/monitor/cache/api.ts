import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  CacheEntry,
  CacheEntryKey,
  CacheOverview,
  CacheRuntime,
  CacheScope,
  CacheScopeFilter,
  CacheValue,
} from './types';

export const cacheApi = {
  overview: () =>
    request.post<Result<CacheOverview>>('/sys/monitor/cache/overview').then((r) => r.data.data),
  runtime: () =>
    request.post<Result<CacheRuntime>>('/sys/monitor/cache/runtime').then((r) => r.data.data),
  scopeTree: () =>
    request.post<Result<CacheScope[]>>('/sys/monitor/cache/scopeTree').then((r) => r.data.data),
  clear: (cacheName: string) =>
    request
      .post<Result<string>>('/sys/monitor/cache/clear', { cacheName })
      .then((r) => r.data.data),
  clearAll: () =>
    request.post<Result<string>>('/sys/monitor/cache/clearAll').then((r) => r.data.data),
  listPage: (
    form: {
      pageNum: number;
      pageSize: number;
      keyword?: string;
    } & CacheScopeFilter,
  ) =>
    request
      .post<Result<PageData<CacheEntry>>>('/sys/monitor/cache/listPage', form)
      .then((r) => r.data.data),
  value: (entry: CacheEntryKey) =>
    request.post<Result<CacheValue>>('/sys/monitor/cache/value', entry).then((r) => r.data.data),
  delete: (entries: CacheEntryKey[]) =>
    request.post<Result<number>>('/sys/monitor/cache/delete', { entries }).then((r) => r.data.data),
};
