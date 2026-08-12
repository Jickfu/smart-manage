import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.basicData,
    featureKey: 'sys/base/basic-data',
    title: '基础资料',
    pageType: 'LIST',
    component: lazy(() => import('./BasicDataListPage')),
  },
  {
    componentKey: componentKeys.basicDataEdit,
    featureKey: 'sys/base/basic-data',
    title: '基础资料',
    pageType: 'EDIT',
    component: lazy(() => import('./BasicDataEditPage')),
  },
]);
