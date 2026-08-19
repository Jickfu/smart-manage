import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/node',
    featureKey: 'sys/monitor/node',
    title: '服务状态',
    pageType: 'CUSTOM',
    component: lazy(() => import('./NodeMonitorPage')),
  },
]);
