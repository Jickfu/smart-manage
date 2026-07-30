import type { PageForm } from '@/types/api';

export type LoginEventType = 'LOGIN' | 'LOGOUT';

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
}

export interface LoginLogDetailVO extends LoginLogListVO {
  userAgent?: string;
}
