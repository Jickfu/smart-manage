import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/permission',
    title: '权限',
    pageType: 'LIST',
    component: lazy(() => import('./PermissionListPage')),
  },
]);
