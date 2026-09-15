import type { PageForm } from '@/types/api';
import type { SchedulerScope } from '../common/schedulerScope';

export type ExecutionStatus = 'RUNNING' | 'SUCCESS' | 'FAILED' | 'SKIPPED';

export interface ExecutionListForm extends PageForm, SchedulerScope {
  keyword?: string;
  status?: ExecutionStatus;
  jobId?: string;
}

export interface ExecutionVO {
  id: string;
  jobId?: string;
  jobName?: string;
  domainId: string;
  domainName: string;
  appId: string;
  appName: string;
  startTime?: string;
  endTime?: string;
  durationMs?: number | null;
  status: ExecutionStatus;
  errorMessage?: string;
  traceId?: string;
  createTime?: string;
}
