import { lazy } from 'react';
import { definePageRegistrations } from '@/domain/common/registry/componentRegistry';
import { componentKeys } from '@/domain/common/registry/componentKeys';

export default definePageRegistrations([
  {
    componentKey: componentKeys.purchaseRequisition,
    pageType: 'LIST',
    component: lazy(() => import('./PurchaseRequisitionListPage')),
  },
  {
    componentKey: componentKeys.purchaseRequisitionEdit,
    pageType: 'EDIT',
    component: lazy(() => import('./PurchaseRequisitionEditPage')),
  },
]);
