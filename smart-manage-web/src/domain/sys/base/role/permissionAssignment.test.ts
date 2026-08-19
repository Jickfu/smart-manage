import { describe, expect, it } from 'vitest';
import type { PermissionListAllVO } from '@/domain/sys/base/permission/types';
import {
  buildPermissionAssignmentTree,
  filterPermissionsByAssignmentScope,
  parsePermissionAssignmentScope,
} from './permissionAssignment';

const permissions: PermissionListAllVO[] = [
  {
    id: '1',
    number: 'sys:base:app:list',
    name: '应用列表',
    appId: '10',
    appName: '系统管理',
  },
  {
    id: '2',
    number: 'sys:base:user:listPage',
    name: '用户列表',
    appId: '10',
    appName: '系统管理',
    featureId: '20',
    featureKey: 'sys/base/user',
    featureName: '用户管理',
  },
  {
    id: '3',
    number: 'scm:purchase:listPage',
    name: '采购申请列表',
    appId: '11',
    appName: '供应链',
    featureId: '21',
    featureKey: 'scm/procurement/purchase-requisition',
    featureName: '采购申请',
  },
];

describe('permissionAssignment', () => {
  it('按应用和功能构建带选中统计的导航树', () => {
    const tree = buildPermissionAssignmentTree(permissions, new Set(['2']));
    const root = tree[0];
    const systemApp = root?.children?.[0];

    expect(root?.title).toBe('全部权限（1/3）');
    expect(systemApp?.title).toBe('系统管理（1/2）');
    expect(systemApp?.children?.map((node) => node.title)).toEqual([
      '应用级权限（0/1）',
      '用户管理（1/1）',
    ]);
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
