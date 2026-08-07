import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/operate-log',
    title: '操作日志',
    pageType: 'LIST',
    component: lazy(() => import('./OperateLogPage')),
  },
  {
    componentKey: 'sys/monitor/operate-log/detail',
    title: '操作日志',
    pageType: 'EDIT',
    component: lazy(() => import('./OperateLogDetailPage')),
  },
]);
