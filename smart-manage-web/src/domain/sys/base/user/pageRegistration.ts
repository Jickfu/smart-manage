import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/user',
    featureKey: 'sys/base/user',
    title: '用户',
    pageType: 'LIST',
    component: lazy(() => import('./UserListPage')),
  },
  {
    componentKey: 'sys/base/user/edit',
    featureKey: 'sys/base/user',
    title: '用户',
    pageType: 'EDIT',
    component: lazy(() => import('./UserEditPage')),
  },
  {
    componentKey: 'sys/base/user/role-assignment',
    featureKey: 'sys/base/user',
    title: '用户角色分配',
    pageType: 'CUSTOM',
    component: lazy(() => import('./UserRoleAssignmentPage')),
  },
]);
