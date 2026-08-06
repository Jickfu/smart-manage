import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.sysParam,
    pageType: 'LIST',
    component: lazy(() => import('./SysParamListPage')),
  },
  {
    componentKey: componentKeys.sysParamEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./SysParamEditPage')),
  },
]);
