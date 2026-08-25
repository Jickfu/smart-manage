import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/alert',
    featureKey: 'sys/monitor/alert',
    title: '监控告警',
    pageType: 'CUSTOM',
    component: lazy(() => import('./MonitorAlertPage')),
  },
]);
