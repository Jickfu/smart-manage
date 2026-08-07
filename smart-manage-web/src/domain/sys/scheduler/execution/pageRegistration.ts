import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.schedulerExecution,
    title: '执行实例',
    pageType: 'LIST',
    component: lazy(() => import('./ExecutionListPage')),
  },
  {
    componentKey: componentKeys.schedulerExecutionDetail,
    title: '执行实例',
    pageType: 'EDIT',
    component: lazy(() => import('./ExecutionDetailPage')),
  },
]);
