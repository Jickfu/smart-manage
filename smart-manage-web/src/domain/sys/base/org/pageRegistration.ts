import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/org',
    title: '组织管理',
    pageType: 'LIST',
    component: lazy(() => import('./OrgListPage')),
  },
]);
