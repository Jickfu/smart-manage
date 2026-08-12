import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/thread',
    featureKey: 'sys/monitor/thread',
    title: '线程诊断',
    pageType: 'CUSTOM',
    component: lazy(() => import('./ThreadDiagnosticPage')),
  },
]);
