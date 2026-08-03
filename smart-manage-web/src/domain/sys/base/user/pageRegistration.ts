import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/user',
    pageType: 'LIST',
    component: lazy(() => import('./UserListPage')),
  },
  {
    componentKey: 'sys/base/user/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./UserEditPage')),
  },
  {
    componentKey: 'sys/base/user/role-assignment',
    pageType: 'CUSTOM',
    component: lazy(() => import('./UserRoleAssignmentPage')),
  },
]);
