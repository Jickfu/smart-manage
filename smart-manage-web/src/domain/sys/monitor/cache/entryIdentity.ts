import type { CacheEntryKey } from './types';

/** 将后端返回的稳定条目标识还原为缓存值查询参数。 */
export function parseCacheEntryIdentity(identity: string): CacheEntryKey {
  const firstSeparator = identity.indexOf('|');
  const secondSeparator = identity.indexOf('|', firstSeparator + 1);
  if (firstSeparator < 1 || secondSeparator < 0) {
    throw new Error('缓存条目标识格式不正确');
  }
  const storage = identity.slice(0, firstSeparator);
  if (storage !== 'LOCAL' && storage !== 'REDIS') {
    throw new Error('缓存存储位置不正确');
  }
  const cacheName = identity.slice(firstSeparator + 1, secondSeparator) || undefined;
  const key = identity.slice(secondSeparator + 1);
  if (!key) {
    throw new Error('缓存 Key 不能为空');
  }
  return { storage, cacheName, key };
}
