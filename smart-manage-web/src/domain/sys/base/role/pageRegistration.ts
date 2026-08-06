import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.role,
    pageType: 'LIST',
    component: lazy(() => import('./RoleListPage')),
  },
  {
    componentKey: componentKeys.roleEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./RoleEditPage')),
  },
  {
    componentKey: componentKeys.rolePermissionAssignment,
    pageType: 'CUSTOM',
    component: lazy(() => import('./RolePermissionAssignmentPage')),
  },
]);
