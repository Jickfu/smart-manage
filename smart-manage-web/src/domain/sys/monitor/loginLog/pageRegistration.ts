import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/login-log',
    title: '登录日志',
    pageType: 'LIST',
    component: lazy(() => import('./LoginLogPage')),
  },
  {
    componentKey: 'sys/monitor/login-log/detail',
    title: '登录日志',
    pageType: 'EDIT',
    component: lazy(() => import('./LoginLogDetailPage')),
  },
]);
