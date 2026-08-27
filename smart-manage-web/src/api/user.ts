import request from './request';
import type { Result, SessionVO, UserInfoVO } from '@/types/api';

/** 恢复当前浏览器登录会话和会话绑定的 CSRF Token。 */
export function getCurrentSession() {
  return request.get<Result<SessionVO>>('/sys/base/session').then((res) => res.data);
}

/** 按业务前缀获取当前用户权限，超级管理员返回通配符。 */
export function getCurrentPermissions(prefix: string) {
  return request
    .post<Result<string[]>>('/sys/base/user/permissions', { prefix })
    .then((response) => response.data.data);
}

/** 保存当前用户的个人主题色。 */
export function updateCurrentUserTheme(themeColor: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/theme', { themeColor })
    .then((response) => response.data);
}

/** 切换到当前用户有效任职范围内的组织。 */
export function switchCurrentUserOrganization(orgId: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/organization', { orgId })
    .then((response) => response.data);
}

export function updateCurrentUserProfile(form: {
  name: string;
  gender?: 'MALE' | 'FEMALE';
  birthday?: string;
  avatarAttachmentId?: string;
  attachmentUploadSessions: Record<string, string>;
}) {
  return request
    .post<Result<UserInfoVO>>('/sys/base/user/current/profile', form)
    .then((response) => response.data.data);
}

export function updateCurrentUserContact(form: {
  verificationMethod: 'PASSWORD';
  password: string;
  type: 'PHONE' | 'EMAIL';
  value: string;
}) {
  return request
    .post<Result<UserInfoVO>>('/sys/base/user/current/contact', form)
    .then((response) => response.data.data);
}

export function getCurrentPasswordPublicKey() {
  return request
    .get<Result<string>>('/sys/base/user/current/password/publicKey')
    .then((response) => response.data.data);
}

export function updateCurrentUserPassword(currentPassword: string, newPassword: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/password', { currentPassword, newPassword })
    .then((response) => response.data);
}

export function requestCurrentPasswordEmailCode() {
  return request
    .post<Result<string>>('/sys/base/user/current/password/email/code')
    .then((response) => response.data);
}

export function updateCurrentUserPasswordByEmail(code: string, newPassword: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/password/email', { code, newPassword })
    .then((response) => response.data);
}

export function requestCurrentEmailCode(password: string, email: string) {
  return request
    .post<Result<string>>('/sys/base/user/current/email/code', { password, email })
    .then((response) => response.data);
}

export function bindCurrentEmail(email: string, code: string) {
  return request
    .post<Result<UserInfoVO>>('/sys/base/user/current/email/bind', { email, code })
    .then((response) => response.data.data);
}

/** 主动注销当前服务端会话。 */
export function logoutCurrentUser() {
  return request.post<Result<void>>('/sys/base/logout').then((response) => response.data);
}
