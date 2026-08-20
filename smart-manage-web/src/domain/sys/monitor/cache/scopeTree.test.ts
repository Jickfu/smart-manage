import { describe, expect, it } from 'vitest';
import { scopeNodeKeyFromFilter, toTreeNode } from './scopeTree';
import type { CacheScope } from './types';

describe('cache scope tree', () => {
  it('builds registered cache hierarchy with stable keys', () => {
    const group: CacheScope = {
      type: 'APPLICATION',
      name: '应用缓存',
      children: [
        {
          type: 'CACHE',
          name: '用户授权',
          resourceKey: 'sys:base:user-authorization',
          children: [],
        },
      ],
    };

    expect(toTreeNode(group)).toMatchObject({
      key: 'application',
      children: [{ key: 'cache:sys:base:user-authorization', isLeaf: true }],
    });
  });

  it('maps filter state back to the selected tree node', () => {
    expect(
      scopeNodeKeyFromFilter({
        scopeType: 'CACHE',
        resourceKey: 'sys:base:user-authorization',
      }),
    ).toBe('cache:sys:base:user-authorization');
    expect(scopeNodeKeyFromFilter({ scopeType: 'INFRASTRUCTURE' })).toBe('infrastructure');
  });
});
