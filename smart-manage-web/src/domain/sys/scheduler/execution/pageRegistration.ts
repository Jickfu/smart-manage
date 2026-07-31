import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/scheduler/execution',
    pageType: 'LIST',
    component: lazy(() => import('./ExecutionListPage')),
  },
  {
    componentKey: 'sys/scheduler/execution/detail',
    pageType: 'EDIT',
    component: lazy(() => import('./ExecutionDetailPage')),
  },
]);
