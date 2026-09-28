import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '../../componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.flowLog,
    featureKey: 'workflow/process/flow-log',
    title: '流转日志',
    pageType: 'LIST',
    component: lazy(() => import('./FlowLogPage')),
  },
]);
