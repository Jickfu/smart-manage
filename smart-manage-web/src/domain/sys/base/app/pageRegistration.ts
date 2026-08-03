import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/app',
    pageType: 'LIST',
    component: lazy(() => import('./AppListPage')),
  },
  {
    componentKey: 'sys/base/app/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./AppEditPage')),
  },
]);
