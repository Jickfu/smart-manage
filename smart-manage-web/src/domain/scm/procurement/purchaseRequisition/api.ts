import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  PurchaseRequisitionCreateNewDataVO,
  PurchaseRequisitionDeleteForm,
  PurchaseRequisitionDetailVO,
  PurchaseRequisitionListForm,
  PurchaseRequisitionListVO,
  PurchaseRequisitionSaveForm,
  PurchaseRequisitionSubmitForm,
  PurchaseRequisitionHomeSummaryVO,
} from './types';
import type { FileArtifactReference } from '@/domain/common/fileArtifactApi';
import type { DataExportLayout } from '@/domain/common/dataExchange/DataExchangeActions';

const baseUrl = '/scm/procurement/purchase-requisition';

export const purchaseRequisitionApi = {
  homeSummary: () =>
    request
      .get<Result<PurchaseRequisitionHomeSummaryVO>>(`${baseUrl}/home-summary`)
      .then((response) => response.data.data),
  listPage: (form: PurchaseRequisitionListForm) =>
    request
      .post<Result<PageData<PurchaseRequisitionListVO>>>(`${baseUrl}/listPage`, form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<PurchaseRequisitionDetailVO>>(`${baseUrl}/detail`, { id })
      .then((response) => response.data.data),
  createNewData: () =>
    request
      .get<Result<PurchaseRequisitionCreateNewDataVO>>(`${baseUrl}/createNewData`)
      .then((response) => response.data.data),
  save: (form: PurchaseRequisitionSaveForm) =>
    request.post<Result<string>>(`${baseUrl}/save`, form).then((response) => response.data.data),
  submit: (form: PurchaseRequisitionSubmitForm) =>
    request.post<Result<string>>(`${baseUrl}/submit`, form).then((response) => response.data.data),
  delete: (form: PurchaseRequisitionDeleteForm) =>
    request.post<Result<string>>(`${baseUrl}/delete`, form).then((response) => response.data.data),
  export: (form: PurchaseRequisitionListForm & { ids?: string[]; layout: DataExportLayout }) =>
    request
      .post<Result<FileArtifactReference>>(`${baseUrl}/export`, form)
      .then((response) => response.data.data),
};
