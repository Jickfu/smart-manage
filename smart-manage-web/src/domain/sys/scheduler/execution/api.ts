import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { ExecutionListForm, ExecutionVO } from './types';

export const executionApi = {
  listPage: (form: ExecutionListForm) =>
    request
      .post<Result<PageData<ExecutionVO>>>('/sys/scheduler/execution/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<ExecutionVO>>('/sys/scheduler/execution/detail', { id })
      .then((response) => response.data.data),
};
