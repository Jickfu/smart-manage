import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';

export default definePageRegistrations([
  {
    componentKey: 'sys/base/sys-param',
    pageType: 'LIST',
    component: lazy(() => import('./SysParamListPage')),
  },
  {
    componentKey: 'sys/base/sys-param/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./SysParamEditPage')),
  },
]);
