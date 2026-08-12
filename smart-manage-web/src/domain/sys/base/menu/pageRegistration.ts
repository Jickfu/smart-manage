import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/menu',
    featureKey: 'sys/base/menu',
    title: '菜单',
    pageType: 'LIST',
    component: lazy(() => import('./MenuListPage')),
  },
  {
    componentKey: 'sys/base/menu/edit',
    featureKey: 'sys/base/menu',
    title: '菜单',
    pageType: 'EDIT',
    component: lazy(() => import('./MenuEditPage')),
  },
]);
