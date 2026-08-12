import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    featureKey: 'sys/base/feature',
    componentKey: 'sys/base/feature',
    title: '功能',
    pageType: 'LIST',
    component: lazy(() => import('./FeatureListPage')),
  },
]);
