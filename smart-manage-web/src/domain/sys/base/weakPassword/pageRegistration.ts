import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.weakPassword,
    featureKey: 'sys/base/weak-password',
    title: '弱口令管理',
    pageType: 'LIST',
    component: lazy(() => import('./WeakPasswordListPage')),
  },
]);
