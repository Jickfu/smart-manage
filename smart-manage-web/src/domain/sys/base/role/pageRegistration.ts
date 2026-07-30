import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/role',
    pageType: 'LIST',
    component: lazy(() => import('./RoleListPage')),
  },
  {
    componentKey: 'sys/base/role/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./RoleEditPage')),
  },
  {
    componentKey: 'sys/base/role/permission-assignment',
    pageType: 'CUSTOM',
    component: lazy(() => import('./RolePermissionAssignmentPage')),
  },
]);
