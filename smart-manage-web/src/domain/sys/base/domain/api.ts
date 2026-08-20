import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  DomainListForm,
  DomainListVO,
  DomainDetailVO,
  DomainSelectForm,
  DomainSelectVO,
  DomainSaveForm,
} from './types';

export const domainApi = {
  listPage: (form: DomainListForm) =>
    request
      .post<Result<PageData<DomainListVO>>>('/sys/base/domain/listPage', form)
      .then((res) => res.data.data),

  /** 基础资料选择器分页查询 */
  select: (form: DomainSelectForm) =>
    request
      .post<Result<PageData<DomainSelectVO>>>('/sys/base/domain/select', form)
      .then((res) => res.data.data),

  detail: (id: string) =>
    request
      .post<Result<DomainDetailVO>>('/sys/base/domain/detail', { id })
      .then((res) => res.data.data),

  save: (form: DomainSaveForm) =>
    request.post<Result<string>>('/sys/base/domain/save', form).then((res) => res.data.data),

  delete: (id: string) =>
    request.post<Result<string>>('/sys/base/domain/delete', { id }).then((res) => res.data.data),

  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<
        Result<string>
      >(enabled ? '/sys/base/domain/enable' : '/sys/base/domain/disable', { ids })
      .then((res) => res.data.data),
};
