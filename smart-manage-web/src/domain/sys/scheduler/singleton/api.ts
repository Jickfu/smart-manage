import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { ListPageQueryParams } from '@/domain/common/page/list/useListPageQuery';
import type { SingletonTask, SingletonCommand, SingletonEvent } from './types';
export const singletonApi = {
  listPage: (form: ListPageQueryParams) =>
    request
      .post<Result<PageData<SingletonTask>>>('/sys/scheduler/singleton/listPage', form)
      .then((response) => response.data.data),
  detail: (number: string) =>
    request
      .post<Result<SingletonTask>>('/sys/scheduler/singleton/detail', { number })
      .then((response) => response.data.data),
  events: (number: string) =>
    request
      .post<Result<SingletonEvent[]>>('/sys/scheduler/singleton/events', { number })
      .then((response) => response.data.data),
  control: (form: SingletonCommand) =>
    request
      .post<Result<string>>('/sys/scheduler/singleton/control', form)
      .then((response) => response.data.data),
};
