import type { Dayjs } from 'dayjs';

export interface AuditLogFilters {
  success?: boolean;
  eventType?: string;
  traceId?: string;
  timeRange?: [Dayjs, Dayjs];
}

export interface AuditLogListParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  success?: boolean;
  traceId?: string;
  beginTime?: string;
  endTime?: string;
}
