import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  ResetPasswordVO,
  TemporaryLoginGrantVO,
  UserDetailVO,
  UserListForm,
  UserListVO,
  UserSaveForm,
  UserRoleAssignmentSaveForm,
  UserRoleAssignmentWorkspaceVO,
} from './types';

export const userApi = {
  listPage: (form: UserListForm) =>
    request
      .post<Result<PageData<UserListVO>>>('/sys/base/user/listPage', form)
      .then((response) => response.data.data),

  detail: (id: string) =>
    request
      .post<Result<UserDetailVO>>('/sys/base/user/detail', { id })
      .then((response) => response.data.data),

  save: (form: UserSaveForm) =>
    request
      .post<Result<string>>('/sys/base/user/save', form)
      .then((response) => response.data.data),

  delete: (id: string) =>
    request
      .post<Result<string>>('/sys/base/user/delete', { id })
      .then((response) => response.data.data),

  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(enabled ? '/sys/base/user/enable' : '/sys/base/user/disable', { ids })
      .then((response) => response.data.data),

  roleAssignmentWorkspace: (userId: string) =>
    request
      .post<Result<UserRoleAssignmentWorkspaceVO>>('/sys/base/user/roleAssignment/workspace', {
        id: userId,
      })
      .then((response) => response.data.data),

  saveRoleAssignment: (form: UserRoleAssignmentSaveForm) =>
    request
      .post<Result<string>>('/sys/base/user/roleAssignment/save', form)
      .then((response) => response.data.data),

  resetPassword: (id: string) =>
    request
      .post<Result<ResetPasswordVO>>('/sys/base/user/resetPassword', { id })
      .then((response) => response.data.data),

  temporaryLoginSafe: () =>
    request
      .get<Result<boolean>>('/sys/base/user/temporaryLogin/safe')
      .then((response) => response.data.data),

  temporaryLoginPublicKey: () =>
    request
      .get<Result<string>>('/sys/base/user/temporaryLogin/publicKey')
      .then((response) => response.data.data),

  openTemporaryLoginSafe: (encryptedPassword: string) =>
    request
      .post<Result<string>>('/sys/base/user/temporaryLogin/safe', {
        password: encryptedPassword,
      })
      .then((response) => response.data.data),

  createTemporaryLoginGrant: (userId: string, reason: string) =>
    request
      .post<Result<TemporaryLoginGrantVO>>('/sys/base/user/temporaryLogin/grant', {
        userId,
        reason,
      })
      .then((response) => response.data.data),
};
