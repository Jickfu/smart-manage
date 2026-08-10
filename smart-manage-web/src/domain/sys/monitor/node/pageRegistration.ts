import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/node',
    title: '运行监控',
    pageType: 'CUSTOM',
    component: lazy(() => import('./NodeMonitorPage')),
  },
]);
