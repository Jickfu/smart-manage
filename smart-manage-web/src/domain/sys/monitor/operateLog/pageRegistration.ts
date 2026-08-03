import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/operate-log',
    pageType: 'LIST',
    component: lazy(() => import('./OperateLogPage')),
  },
  {
    componentKey: 'sys/monitor/operate-log/detail',
    pageType: 'EDIT',
    component: lazy(() => import('./OperateLogDetailPage')),
  },
]);
