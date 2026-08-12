import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.sysParam,
    featureKey: 'sys/base/param',
    title: '系统参数',
    pageType: 'LIST',
    component: lazy(() => import('./SysParamListPage')),
  },
  {
    componentKey: componentKeys.sysParamEdit,
    featureKey: 'sys/base/param',
    title: '系统参数',
    pageType: 'EDIT',
    component: lazy(() => import('./SysParamEditPage')),
  },
]);
