import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '../../componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.instance,
    featureKey: 'workflow/process/instance',
    title: '流程实例',
    pageType: 'LIST',
    component: lazy(() => import('./InstanceListPage')),
  },
  {
    componentKey: componentKeys.instanceDetail,
    featureKey: 'workflow/process/instance',
    title: '流程实例',
    pageType: 'EDIT',
    component: lazy(() => import('./InstanceDetailPage')),
  },
]);
