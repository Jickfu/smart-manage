import type { PageForm } from '@/types/api';

export type LoginEventType =
  | 'LOGIN_SUCCESS'
  | 'LOGIN_FAILURE'
  | 'PASSWORD_CHANGE_REQUIRED'
  | 'LOGOUT'
  | 'SESSION_KICKED'
  | 'SESSION_REPLACED'
  | 'ACCOUNT_DISABLED'
  | 'PASSWORD_RESET_TERMINATED'
  | 'TEMPORARY_LOGIN_GRANT_CREATED'
  | 'TEMPORARY_LOGIN_SUCCESS';

export interface LoginLogListForm extends PageForm {
  keyword?: string;
  success?: boolean;
  eventType?: LoginEventType;
  traceId?: string;
  beginTime?: string;
  endTime?: string;
}

export interface LoginLogListVO {
  id: string;
  userId?: string;
  username?: string;
  nickname?: string;
  eventType: LoginEventType;
  success: boolean;
  failReason?: string;
  ip?: string;
  traceId?: string;
  createTime: string;
  issuerUserId?: string;
  grantId?: string;
}

export interface LoginLogDetailVO extends LoginLogListVO {
  userAgent?: string;
  grantReason?: string;
  grantExpiresAt?: string;
}
