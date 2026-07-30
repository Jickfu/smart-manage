import type { PageForm } from '@/types/api';

export interface OperateLogListForm extends PageForm {
  keyword?: string;
  success?: boolean;
  traceId?: string;
  beginTime?: string;
  endTime?: string;
}

export interface OperateLogListVO {
  id: string;
  bizName?: string;
  success: boolean;
  errorMsg?: string;
  requestMethod?: string;
  requestUri?: string;
  ip?: string;
  className?: string;
  methodName?: string;
  durationMs?: number;
  username?: string;
  traceId?: string;
  createTime: string;
}

export interface OperateLogDetailVO extends OperateLogListVO {
  userAgent?: string;
  requestParams?: string;
  responseBody?: string;
  userId?: string;
}
