import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  CloudListForm,
  CloudListVO,
  CloudDetailVO,
  CloudSelectForm,
  CloudSelectVO,
  CloudSaveForm,
} from './types';

export const cloudApi = {
  listPage: (form: CloudListForm) =>
    request
      .post<Result<PageData<CloudListVO>>>('/sys/base/cloud/listPage', form)
      .then((res) => res.data.data),

  /** 基础资料选择器分页查询 */
  select: (form: CloudSelectForm) =>
    request
      .post<Result<PageData<CloudSelectVO>>>('/sys/base/cloud/select', form)
      .then((res) => res.data.data),

  detail: (id: string) =>
    request
      .post<Result<CloudDetailVO>>('/sys/base/cloud/detail', { id })
      .then((res) => res.data.data),

  save: (form: CloudSaveForm) =>
    request.post<Result<string>>('/sys/base/cloud/save', form).then((res) => res.data.data),

  delete: (id: string) =>
    request.post<Result<string>>('/sys/base/cloud/delete', { id }).then((res) => res.data.data),

  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(enabled ? '/sys/base/cloud/enable' : '/sys/base/cloud/disable', { ids })
      .then((res) => res.data.data),
};
