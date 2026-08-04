import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/sql-console',
    pageType: 'CUSTOM',
    component: lazy(() => import('./SqlConsolePage')),
  },
  {
    componentKey: 'sys/monitor/sql-log',
    pageType: 'LIST',
    component: lazy(() => import('./SqlLogPage')),
  },
  {
    componentKey: 'sys/monitor/sql-log/detail',
    pageType: 'EDIT',
    component: lazy(() => import('./SqlLogDetailPage')),
  },
]);
