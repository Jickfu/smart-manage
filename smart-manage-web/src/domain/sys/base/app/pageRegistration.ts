import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/app',
    title: '应用',
    pageType: 'LIST',
    component: lazy(() => import('./AppListPage')),
  },
  {
    componentKey: 'sys/base/app/edit',
    title: '应用',
    pageType: 'EDIT',
    component: lazy(() => import('./AppEditPage')),
  },
]);
