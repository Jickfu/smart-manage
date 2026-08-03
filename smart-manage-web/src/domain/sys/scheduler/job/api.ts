import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { JobClassOption, JobCommand, JobListForm, JobSaveForm, JobVO } from './types';

const postCommand = (path: string, id: string, version: number) =>
  request.post<Result<string>>(path, { id, version }).then((response) => response.data.data);

const postBatchCommand = (path: string, jobs: JobCommand[]) =>
  request.post<Result<string>>(path, { jobs }).then((response) => response.data.data);

export const jobApi = {
  listPage: (form: JobListForm) =>
    request
      .post<Result<PageData<JobVO>>>('/sys/scheduler/job/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<JobVO>>('/sys/scheduler/job/detail', { id })
      .then((response) => response.data.data),
  save: (form: JobSaveForm) =>
    request
      .post<Result<string>>('/sys/scheduler/job/save', form)
      .then((response) => response.data.data),
  delete: (id: string, version: number) => postCommand('/sys/scheduler/job/delete', id, version),
  pause: (jobs: JobCommand[]) => postBatchCommand('/sys/scheduler/job/pause', jobs),
  resume: (jobs: JobCommand[]) => postBatchCommand('/sys/scheduler/job/resume', jobs),
  trigger: (id: string) =>
    request
      .post<Result<string>>('/sys/scheduler/job/trigger', { id })
      .then((response) => response.data.data),
  syncAll: () =>
    request
      .post<Result<string>>('/sys/scheduler/job/syncAll')
      .then((response) => response.data.data),
  classes: () =>
    request
      .post<Result<JobClassOption[]>>('/sys/scheduler/job/classes')
      .then((response) => response.data.data),
  cronPreview: (cronExpression: string) =>
    request
      .post<Result<string[]>>('/sys/scheduler/job/cronPreview', { cronExpression })
      .then((response) => response.data.data),
  createNewData: () =>
    request
      .get<Result<Partial<JobVO>>>('/sys/scheduler/job/createNewData')
      .then((response) => response.data.data),
};
