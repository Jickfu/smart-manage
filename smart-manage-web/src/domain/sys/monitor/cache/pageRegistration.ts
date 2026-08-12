import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/monitor/cache-status',
    featureKey: 'sys/monitor/cache',
    title: '缓存状态',
    pageType: 'CUSTOM',
    component: lazy(() => import('./CachePage')),
  },
  {
    componentKey: 'sys/monitor/cache-value',
    featureKey: 'sys/monitor/cache',
    title: '缓存值',
    pageType: 'EDIT',
    component: lazy(() => import('./CacheValuePage')),
  },
  {
    componentKey: 'sys/monitor/cache-management',
    featureKey: 'sys/monitor/cache',
    title: '缓存管理',
    pageType: 'CUSTOM',
    component: lazy(() => import('./CacheManagementPage')),
  },
]);
