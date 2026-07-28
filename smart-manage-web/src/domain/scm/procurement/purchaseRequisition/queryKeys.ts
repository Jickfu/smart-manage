import type { PurchaseRequisitionListForm } from './types';

export const purchaseRequisitionQueryKeys = {
  all: ['scm', 'procurement', 'purchase-requisition'] as const,
  lists: () => [...purchaseRequisitionQueryKeys.all, 'list'] as const,
  list: (params: Partial<PurchaseRequisitionListForm>) =>
    [...purchaseRequisitionQueryKeys.lists(), params] as const,
  details: () => [...purchaseRequisitionQueryKeys.all, 'detail'] as const,
  detail: (id?: string) => [...purchaseRequisitionQueryKeys.details(), id] as const,
  createNewData: (tabKey: string) =>
    [...purchaseRequisitionQueryKeys.all, 'create-new-data', tabKey] as const,
};
