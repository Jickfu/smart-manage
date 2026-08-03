import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/basic-data',
    pageType: 'LIST',
    component: lazy(() => import('./BasicDataListPage')),
  },
  {
    componentKey: 'sys/base/basic-data/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./BasicDataEditPage')),
  },
]);
