import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { SysParamDetailVO, SysParamListForm, SysParamSaveForm, SysParamVO } from './types';

export const sysParamApi = {
  listPage: (form: SysParamListForm) =>
    request
      .post<Result<PageData<SysParamVO>>>('/sys/base/param/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<SysParamDetailVO>>('/sys/base/param/detail', { id })
      .then((response) => response.data.data),
  save: (form: SysParamSaveForm) =>
    request
      .post<Result<string>>('/sys/base/param/save', form)
      .then((response) => response.data.data),
  delete: (id: string) =>
    request
      .post<Result<string>>('/sys/base/param/delete', { id })
      .then((response) => response.data.data),
};
