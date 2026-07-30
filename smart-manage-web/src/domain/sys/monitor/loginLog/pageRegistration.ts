import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/login-log',
    pageType: 'LIST',
    component: lazy(() => import('./LoginLogPage')),
  },
  {
    componentKey: 'sys/monitor/login-log/detail',
    pageType: 'EDIT',
    component: lazy(() => import('./LoginLogDetailPage')),
  },
]);
