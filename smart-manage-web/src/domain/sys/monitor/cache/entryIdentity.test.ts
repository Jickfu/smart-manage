import { describe, expect, it } from 'vitest';
import { parseCacheEntryIdentity } from './entryIdentity';

describe('parseCacheEntryIdentity', () => {
  it('保留 Redis Key 中的分隔符', () => {
    expect(parseCacheEntryIdentity('REDIS||session|token|1')).toEqual({
      storage: 'REDIS',
      cacheName: undefined,
      key: 'session|token|1',
    });
  });

  it('还原本地缓存名称和 Key', () => {
    expect(parseCacheEntryIdentity('LOCAL|sys-param|theme')).toEqual({
      storage: 'LOCAL',
      cacheName: 'sys-param',
      key: 'theme',
    });
  });

  it('拒绝非法标识', () => {
    expect(() => parseCacheEntryIdentity('invalid')).toThrow('缓存条目标识格式不正确');
  });
});
