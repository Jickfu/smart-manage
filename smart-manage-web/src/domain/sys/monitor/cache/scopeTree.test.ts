import { describe, expect, it } from 'vitest';
import { scopeNodeKeyFromFilter, toTreeNode } from './scopeTree';
import type { CacheScope } from './types';

describe('cache scope tree', () => {
  it('builds domain and application hierarchy with stable keys', () => {
    const domain: CacheScope = {
      type: 'DOMAIN',
      name: '系统服务',
      domainNumber: 'sys',
      children: [
        {
          type: 'APP',
          name: '系统管理',
          domainNumber: 'sys',
          appNumber: 'base',
          children: [],
        },
      ],
    };

    expect(toTreeNode(domain)).toMatchObject({
      key: 'domain:sys',
      children: [{ key: 'app:sys:base', isLeaf: true }],
    });
  });

  it('maps filter state back to the selected tree node', () => {
    expect(
      scopeNodeKeyFromFilter({ scopeType: 'APP', domainNumber: 'sys', appNumber: 'base' }),
    ).toBe('app:sys:base');
    expect(scopeNodeKeyFromFilter({ scopeType: 'OTHER' })).toBe('other');
  });
});
