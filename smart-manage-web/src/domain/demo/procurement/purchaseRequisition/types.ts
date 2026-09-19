import type { PageForm } from '@/types/api';
import type { BusinessAttachment } from '@/domain/common/attachment/types';

export interface PurchaseRequisitionEntry {
  id?: string;
  materialName: string;
  specification?: string;
  unit: string;
  quantity: number;
  requiredDate?: string;
  remark?: string;
  sort?: number;
}

export interface PurchaseRequisitionListForm extends PageForm {
  keyword?: string;
  billStatus?: string;
}

export interface PurchaseRequisitionListVO {
  id: string;
  version: number;
  number: string;
  subject: string;
  bizDate: string;
  requiredDate?: string;
  billStatus: string;
  createTime?: string;
}

export interface PurchaseRequisitionDeleteForm {
  id: string;
  version: number;
}

export interface PurchaseRequisitionDetailVO {
  id: string;
  version: number;
  number: string;
  subject: string;
  orgId: string;
  applicantId: string;
  bizDate: string;
  requiredDate?: string;
  reason?: string;
  billStatus: string;
  createTime?: string;
  updateTime?: string;
  entries: PurchaseRequisitionEntry[];
  attachments: BusinessAttachment[];
}

export interface PurchaseRequisitionCreateNewDataVO {
  orgId: string;
  applicantId: string;
  bizDate: string;
  billStatus: string;
  entries: PurchaseRequisitionEntry[];
  attachments: BusinessAttachment[];
}

export interface PurchaseRequisitionSaveForm {
  id?: string;
  version?: number;
  number?: string;
  subject: string;
  bizDate: string;
  requiredDate?: string;
  reason?: string;
  attachmentIds?: string[];
  attachmentUploadSessions?: Record<string, string>;
  entries: PurchaseRequisitionEntry[];
}

export type PurchaseRequisitionSubmitForm = PurchaseRequisitionSaveForm;

export interface PurchaseRequisitionHomeSummaryVO {
  statusCounts: Record<string, number>;
  recent: PurchaseRequisitionListVO[];
}
