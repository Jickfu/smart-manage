import type { PageForm } from '@/types/api';

export type ExecutionStatus = 'RUNNING' | 'SUCCESS' | 'FAILED' | 'SKIPPED';

export interface ExecutionListForm extends PageForm {
  keyword?: string;
  status?: ExecutionStatus;
  jobId?: string;
}

export interface ExecutionVO {
  id: string;
  jobId?: string;
  jobName?: string;
  jobGroup?: string;
  startTime?: string;
  endTime?: string;
  durationMs?: number;
  status: ExecutionStatus;
  errorMessage?: string;
  traceId?: string;
  createTime?: string;
}
