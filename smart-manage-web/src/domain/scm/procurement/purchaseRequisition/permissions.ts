import { defineAccessResource } from '@/domain/common/page/access/access';

export const purchaseRequisitionAccess = defineAccessResource(
  'scm:procurement:purchase-requisition',
  {
    list: 'listPage',
    detail: 'detail',
    save: 'save',
    submit: 'submit',
    delete: 'delete',
    export: 'export',
  },
);
