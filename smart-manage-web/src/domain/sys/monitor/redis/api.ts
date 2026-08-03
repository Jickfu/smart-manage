import request from '@/api/request';
import type { Result } from '@/types/api';
import type { RedisKeys, RedisRuntime, RedisValue } from './types';

export const redisApi = {
  runtime: () =>
    request.post<Result<RedisRuntime>>('/sys/monitor/redis/runtime').then((r) => r.data.data),
  keys: (form: { cursor: string; pattern: string; count: number }) =>
    request.post<Result<RedisKeys>>('/sys/monitor/redis/keys', form).then((r) => r.data.data),
  value: (key: string) =>
    request.post<Result<RedisValue>>('/sys/monitor/redis/value', { key }).then((r) => r.data.data),
  delete: (keys: string[]) =>
    request.post<Result<number>>('/sys/monitor/redis/delete', { keys }).then((r) => r.data.data),
};
