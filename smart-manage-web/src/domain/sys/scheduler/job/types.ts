import type { PageForm } from '@/types/api';
import type { ReferenceVO } from '@/domain/sys/base/common/types';
import type { SchedulerScope } from '../common/schedulerScope';

export type JobStatus = 'ENABLED' | 'PAUSED';

export interface JobListForm extends PageForm, SchedulerScope {
  keyword?: string;
  status?: JobStatus;
}

export interface JobVO {
  id: string;
  version: number;
  number: string;
  jobName: string;
  appId: string;
  appName: string;
  domainId: string;
  domainName: string;
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

export interface JobDetailVO extends Omit<JobVO, 'appId' | 'appName' | 'domainId' | 'domainName'> {
  app: ReferenceVO;
}

export interface JobSaveForm {
  id?: string;
  version?: number;
  number: string;
  jobName: string;
  appId: string;
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
  appId?: string;
  appNumber?: string;
  appName?: string;
}
