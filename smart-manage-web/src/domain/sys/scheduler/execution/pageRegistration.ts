import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.schedulerExecution,
    pageType: 'LIST',
    component: lazy(() => import('./ExecutionListPage')),
  },
  {
    componentKey: componentKeys.schedulerExecutionDetail,
    pageType: 'EDIT',
    component: lazy(() => import('./ExecutionDetailPage')),
  },
]);
