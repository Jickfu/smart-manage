import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { OperateLogDetailVO, OperateLogListForm, OperateLogListVO } from './types';

export const operateLogApi = {
  listPage: (form: OperateLogListForm) =>
    request
      .post<Result<PageData<OperateLogListVO>>>('/sys/log/operate/listPage', form)
      .then((response) => response.data.data),

  currentListPage: (form: OperateLogListForm) =>
    request
      .post<Result<PageData<OperateLogListVO>>>('/sys/log/operate/current/listPage', form)
      .then((response) => response.data.data),

  detail: (id: string) =>
    request
      .post<Result<OperateLogDetailVO>>('/sys/log/operate/detail', { id })
      .then((response) => response.data.data),
};
