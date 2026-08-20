import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/domain',
    featureKey: 'sys/base/domain',
    title: '领域',
    pageType: 'LIST',
    component: lazy(() => import('./DomainListPage')),
  },
]);
