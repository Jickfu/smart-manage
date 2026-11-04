import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';
export default definePageRegistrations([
  {
    componentKey: componentKeys.schedulerSingleton,
    featureKey: 'sys/scheduler/singleton',
    title: '常驻任务',
    pageType: 'LIST',
    component: lazy(() => import('./SingletonListPage')),
  },
  {
    componentKey: componentKeys.schedulerSingletonDetail,
    featureKey: 'sys/scheduler/singleton',
    title: '常驻任务',
    pageType: 'EDIT',
    component: lazy(() => import('./SingletonDetailPage')),
  },
]);
