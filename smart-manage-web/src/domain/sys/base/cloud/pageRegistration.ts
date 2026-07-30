import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/cloud',
    pageType: 'LIST',
    component: lazy(() => import('./CloudListPage')),
  },
]);
