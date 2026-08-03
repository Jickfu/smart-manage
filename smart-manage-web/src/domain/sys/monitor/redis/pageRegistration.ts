import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/redis',
    pageType: 'CUSTOM',
    component: lazy(() => import('./RedisPage')),
  },
]);
