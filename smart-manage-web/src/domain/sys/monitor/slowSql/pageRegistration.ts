import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/slow-sql',
    featureKey: 'sys/monitor/slow-sql',
    title: '慢 SQL 监控',
    pageType: 'CUSTOM',
    component: lazy(() => import('./SlowSqlMonitorPage')),
  },
]);
