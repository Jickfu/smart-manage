import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/scheduler/job',
    pageType: 'LIST',
    component: lazy(() => import('./JobListPage')),
  },
  {
    componentKey: 'sys/scheduler/job/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./JobEditPage')),
  },
]);
