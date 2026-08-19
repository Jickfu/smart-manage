import { describe, expect, it } from 'vitest';
import { scopeNodeKeyFromFilter, toTreeNode } from './scopeTree';
import type { CacheScope } from './types';

describe('cache scope tree', () => {
  it('builds cloud and application hierarchy with stable keys', () => {
    const cloud: CacheScope = {
      type: 'CLOUD',
      name: '系统服务',
      cloudNumber: 'sys',
      children: [
        {
          type: 'APP',
          name: '系统管理',
          cloudNumber: 'sys',
          appNumber: 'base',
          children: [],
        },
      ],
    };

    expect(toTreeNode(cloud)).toMatchObject({
      key: 'cloud:sys',
      children: [{ key: 'app:sys:base', isLeaf: true }],
    });
  });

  it('maps filter state back to the selected tree node', () => {
    expect(
      scopeNodeKeyFromFilter({ scopeType: 'APP', cloudNumber: 'sys', appNumber: 'base' }),
    ).toBe('app:sys:base');
    expect(scopeNodeKeyFromFilter({ scopeType: 'OTHER' })).toBe('other');
  });
});
