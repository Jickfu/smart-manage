import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { LoginLogDetailVO, LoginLogListForm, LoginLogListVO } from './types';

export const loginLogApi = {
  listPage: (form: LoginLogListForm) =>
    request
      .post<Result<PageData<LoginLogListVO>>>('/sys/log/login/listPage', form)
      .then((response) => response.data.data),

  currentListPage: (form: LoginLogListForm) =>
    request
      .post<Result<PageData<LoginLogListVO>>>('/sys/log/login/current/listPage', form)
      .then((response) => response.data.data),

  detail: (id: string) =>
    request
      .post<Result<LoginLogDetailVO>>('/sys/log/login/detail', { id })
      .then((response) => response.data.data),
};
