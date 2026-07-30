import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  RoleDetailVO,
  RoleListAllVO,
  RoleListForm,
  RoleListVO,
  RoleSaveForm,
  RoleSelectVO,
} from './types';

export const roleApi = {
  listPage: (form: RoleListForm) =>
    request
      .post<Result<PageData<RoleListVO>>>('/sys/base/role/listPage', form)
      .then((response) => response.data.data),

  listAll: () =>
    request
      .post<Result<RoleListAllVO[]>>('/sys/base/role/listAll', {})
      .then((response) => response.data.data),

  select: (form: { pageNum: number; pageSize: number; keyword?: string }) =>
    request
      .post<Result<PageData<RoleSelectVO>>>('/sys/base/role/select', form)
      .then((response) => response.data.data),

  detail: (id: string) =>
    request
      .post<Result<RoleDetailVO>>('/sys/base/role/detail', { id })
      .then((response) => response.data.data),

  save: (form: RoleSaveForm) =>
    request
      .post<Result<string>>('/sys/base/role/save', form)
      .then((response) => response.data.data),

  delete: (id: string) =>
    request
      .post<Result<string>>('/sys/base/role/delete', { id })
      .then((response) => response.data.data),

  assignPermissions: (roleId: string, permissionIds: string[]) =>
    request
      .post<Result<string>>('/sys/base/role/assignPermissions', { roleId, permissionIds })
      .then((response) => response.data.data),
};
