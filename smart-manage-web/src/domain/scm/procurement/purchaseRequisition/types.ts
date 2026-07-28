import type { PageForm } from '@/types/api';

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
  applyOrgId: string;
  applicantId: string;
  bizDate: string;
  requiredDate?: string;
  reason?: string;
  billStatus: string;
  createTime?: string;
  updateTime?: string;
  entrys: PurchaseRequisitionEntry[];
}

export interface PurchaseRequisitionCreateNewDataVO {
  applyOrgId: string;
  applicantId: string;
  bizDate: string;
  billStatus: string;
  entrys: PurchaseRequisitionEntry[];
}

export interface PurchaseRequisitionSaveForm {
  id?: string;
  version?: number;
  number: string;
  subject: string;
  bizDate: string;
  requiredDate?: string;
  reason?: string;
  entrys: PurchaseRequisitionEntry[];
}

export type PurchaseRequisitionSubmitForm = PurchaseRequisitionSaveForm;
