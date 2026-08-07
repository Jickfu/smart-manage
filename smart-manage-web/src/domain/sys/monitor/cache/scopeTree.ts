import type { DataNode } from 'antd/es/tree';
import type { CacheScope, CacheScopeFilter } from './types';

export function scopeNodeKey(scope: CacheScope): string {
  if (scope.type === 'CLOUD') return `cloud:${scope.cloudNumber}`;
  if (scope.type === 'APP') return `app:${scope.cloudNumber}:${scope.appNumber}`;
  return 'other';
}

export function toTreeNode(scope: CacheScope): DataNode {
  return {
    key: scopeNodeKey(scope),
    title: scope.name,
    isLeaf: scope.type !== 'CLOUD',
    children: scope.children.map(toTreeNode),
  };
}

export function scopeNodeKeyFromFilter(scope: CacheScopeFilter): string {
  if (scope.scopeType === 'CLOUD') return `cloud:${scope.cloudNumber}`;
  if (scope.scopeType === 'APP') return `app:${scope.cloudNumber}:${scope.appNumber}`;
  if (scope.scopeType === 'OTHER') return 'other';
  return 'all';
}
