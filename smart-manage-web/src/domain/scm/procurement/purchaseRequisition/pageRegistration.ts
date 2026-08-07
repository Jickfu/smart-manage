import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.purchaseRequisition,
    title: '采购申请',
    pageType: 'LIST',
    component: lazy(() => import('./PurchaseRequisitionListPage')),
  },
  {
    componentKey: componentKeys.purchaseRequisitionEdit,
    title: '采购申请',
    pageType: 'EDIT',
    component: lazy(() => import('./PurchaseRequisitionEditPage')),
  },
]);
