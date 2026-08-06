import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.schedulerJob,
    pageType: 'LIST',
    component: lazy(() => import('./JobListPage')),
  },
  {
    componentKey: componentKeys.schedulerJobEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./JobEditPage')),
  },
]);
