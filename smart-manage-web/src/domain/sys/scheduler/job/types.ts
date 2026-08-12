import type { PageForm } from '@/types/api';

export type JobStatus = 'ENABLED' | 'PAUSED';

export interface JobListForm extends PageForm {
  keyword?: string;
  status?: JobStatus;
}

export interface JobVO {
  id: string;
  version: number;
  number: string;
  jobName: string;
  jobGroup: string;
  jobClassName: string;
  cronExpression: string;
  jobData?: string;
  mutexKey?: string;
  status: JobStatus;
  description?: string;
  isSystem: boolean;
  nextFireTime?: string;
  lastExecuteTime?: string;
  lastExecuteStatus?: string;
  createTime?: string;
  updateTime?: string;
}

export interface JobSaveForm {
  id?: string;
  version?: number;
  number: string;
  jobName: string;
  jobGroup: string;
  jobClassName: string;
  cronExpression: string;
  jobData?: string;
  mutexKey?: string;
  description?: string;
}

export interface JobCommand {
  id: string;
  version: number;
}

export interface JobClassOption {
  className: string;
  simpleName: string;
  description: string;
  parameterTemplate: string;
}
