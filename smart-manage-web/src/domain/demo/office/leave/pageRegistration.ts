import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '../../componentKeys';
export default definePageRegistrations([
  {
    componentKey: componentKeys.leave,
    featureKey: 'demo/office/leave',
    title: '请假申请',
    pageType: 'LIST',
    component: lazy(() => import('./LeaveListPage')),
  },
  {
    componentKey: componentKeys.leaveEdit,
    featureKey: 'demo/office/leave',
    title: '请假申请',
    pageType: 'EDIT',
    component: lazy(() => import('./LeaveEditPage')),
  },
]);
