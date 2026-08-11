import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  OrgDetailVO,
  OrgListForm,
  OrgListVO,
  OrgParentListForm,
  OrgOptionVO,
  OrgSaveForm,
  OrgTreeNode,
} from './types';

export const orgApi = {
  options: () =>
    request
      .get<Result<OrgOptionVO[]>>('/sys/base/org/options')
      .then((response) => response.data.data),
  tree: (showArchived = false) =>
    request
      .get<Result<OrgTreeNode[]>>('/sys/base/org/tree', { params: { showArchived } })
      .then((response) => response.data.data),
  listPage: (form: OrgListForm) =>
    request
      .post<Result<PageData<OrgListVO>>>('/sys/base/org/listPage', form)
      .then((response) => response.data.data),
  parentListPage: (form: OrgParentListForm) =>
    request
      .post<Result<PageData<OrgListVO>>>('/sys/base/org/parentListPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<OrgDetailVO>>('/sys/base/org/detail', { id })
      .then((response) => response.data.data),
  save: (form: OrgSaveForm) =>
    request.post<Result<string>>('/sys/base/org/save', form).then((response) => response.data.data),
  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(enabled ? '/sys/base/org/enable' : '/sys/base/org/disable', { ids })
      .then((response) => response.data.data),
  setArchived: (ids: string[], archived: boolean) =>
    request
      .post<Result<string>>(archived ? '/sys/base/org/archive' : '/sys/base/org/unarchive', { ids })
      .then((response) => response.data.data),
};
