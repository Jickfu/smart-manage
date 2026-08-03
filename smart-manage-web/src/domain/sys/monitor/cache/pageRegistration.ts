import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/cache-status',
    pageType: 'CUSTOM',
    component: lazy(() => import('./CachePage')),
  },
]);
