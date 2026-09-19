import { describe, expect, it } from 'vitest';
import type { PermissionListAllVO } from '@/domain/sys/base/permission/types';
import {
  buildPermissionAssignmentTree,
  filterPermissionsByAssignmentScope,
  parsePermissionAssignmentScope,
  permissionAssignmentScopeKey,
  getPermissionAssignmentScopeLabel,
} from './permissionAssignment';

const permissions: PermissionListAllVO[] = [
  {
    id: '1',
    number: 'sys:base:app:list',
    name: '应用列表',
    appId: '10',
    appName: '系统管理',
    domainId: '100',
    domainName: '系统领域',
  },
  {
    id: '2',
    number: 'sys:base:user:listPage',
    name: '用户列表',
    appId: '10',
    appName: '系统管理',
    domainId: '100',
    domainName: '系统领域',
    featureId: '20',
    featureKey: 'sys/base/user',
    featureName: '用户管理',
  },
  {
    id: '3',
    number: 'demo:purchase:listPage',
    name: '采购申请列表',
    appId: '11',
    appName: '演示',
    domainId: '101',
    domainName: '演示领域',
    featureId: '21',
    featureKey: 'demo/procurement/purchase-requisition',
    featureName: '采购申请',
  },
];

describe('permissionAssignment', () => {
  it('按领域、应用和功能构建带选中统计的导航树', () => {
    const tree = buildPermissionAssignmentTree(permissions, new Set(['2']));
    const root = tree[0];
    const systemDomain = root?.children?.[0];
    const systemApp = systemDomain?.children?.[0];

    expect(root?.title).toBe('全部权限（1/3）');
    expect(systemDomain?.key).toBe('domain:100');
    expect(systemDomain?.title).toBe('系统领域（1/2）');
    expect(root?.children?.[1]?.title).toBe('演示领域（0/1）');
    expect(systemApp?.title).toBe('系统管理（1/2）');
    expect(systemApp?.children?.map((node) => node.title)).toEqual([
      '应用级权限（0/1）',
      '用户管理（1/1）',
    ]);
  });

  it('领域范围汇总多个应用，按领域 ID 隔离同名领域且不从权限编码推断归属', () => {
    const crossAppPermissions = [
      ...permissions,
      { ...permissions[2]!, id: '4', appId: '12', domainId: '100', domainName: '系统领域' },
      { ...permissions[2]!, id: '5', appId: '13', domainId: '102', domainName: '系统领域' },
    ];
    const scope = { type: 'domain', domainId: '100' } as const;
    expect(
      filterPermissionsByAssignmentScope(crossAppPermissions, scope).map(
        (permission) => permission.id,
      ),
    ).toEqual(['1', '2', '4']);
    const domain = buildPermissionAssignmentTree(crossAppPermissions, new Set(['2', '4', '5']))[0]
      ?.children?.[0];
    expect(domain?.title).toBe('系统领域（2/3）');
    expect(domain?.children?.map((node) => node.key)).toEqual(['app:10', 'app:12']);
    expect(getPermissionAssignmentScopeLabel(crossAppPermissions, scope)).toBe('系统领域');
    expect(parsePermissionAssignmentScope(permissionAssignmentScopeKey(scope))).toEqual(scope);
  });

  it('应用级范围不会混入功能权限', () => {
    expect(
      filterPermissionsByAssignmentScope(permissions, { type: 'app-level', appId: '10' }).map(
        (permission) => permission.id,
      ),
    ).toEqual(['1']);
  });

  it('解析稳定的范围节点键', () => {
    expect(parsePermissionAssignmentScope('feature:20')).toEqual({
      type: 'feature',
      featureId: '20',
    });
  });
});
