import type { DataNode } from 'antd/es/tree';
import type { CacheScope, CacheScopeFilter } from './types';

export function scopeNodeKey(scope: CacheScope): string {
  if (scope.resourceKey) return `${scope.type.toLowerCase()}:${scope.resourceKey}`;
  return scope.type.toLowerCase();
}

export function toTreeNode(scope: CacheScope): DataNode {
  return {
    key: scopeNodeKey(scope),
    title: scope.name,
    isLeaf: scope.children.length === 0,
    children: scope.children.map(toTreeNode),
  };
}

export function scopeNodeKeyFromFilter(scope: CacheScopeFilter): string {
  if (scope.resourceKey) return `${scope.scopeType.toLowerCase()}:${scope.resourceKey}`;
  if (scope.scopeType !== 'ALL') return scope.scopeType.toLowerCase();
  return 'all';
}
