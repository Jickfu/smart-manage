import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/menu',
    pageType: 'LIST',
    component: lazy(() => import('./MenuListPage')),
  },
  {
    componentKey: 'sys/base/menu/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./MenuEditPage')),
  },
]);
