import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.schedulerExecution,
    featureKey: 'sys/scheduler/execution',
    title: '执行记录',
    pageType: 'LIST',
    component: lazy(() => import('./ExecutionListPage')),
  },
  {
    componentKey: componentKeys.schedulerExecutionDetail,
    featureKey: 'sys/scheduler/execution',
    title: '执行记录',
    pageType: 'EDIT',
    component: lazy(() => import('./ExecutionDetailPage')),
  },
]);
