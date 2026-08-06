import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.basicData,
    pageType: 'LIST',
    component: lazy(() => import('./BasicDataListPage')),
  },
  {
    componentKey: componentKeys.basicDataEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./BasicDataEditPage')),
  },
]);
