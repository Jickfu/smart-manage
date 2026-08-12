import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.role,
    featureKey: 'sys/base/role',
    title: '角色',
    pageType: 'LIST',
    component: lazy(() => import('./RoleListPage')),
  },
  {
    componentKey: componentKeys.roleEdit,
    featureKey: 'sys/base/role',
    title: '角色',
    pageType: 'EDIT',
    component: lazy(() => import('./RoleEditPage')),
  },
  {
    componentKey: componentKeys.rolePermissionAssignment,
    featureKey: 'sys/base/role',
    title: '角色权限分配',
    pageType: 'CUSTOM',
    component: lazy(() => import('./RolePermissionAssignmentPage')),
  },
]);
