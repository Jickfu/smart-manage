import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '../../componentKeys';
export default definePageRegistrations([
  {
    componentKey: componentKeys.task,
    featureKey: 'workflow/process/task',
    title: '任务中心',
    pageType: 'CUSTOM',
    component: lazy(() => import('./TaskPage')),
  },
]);
