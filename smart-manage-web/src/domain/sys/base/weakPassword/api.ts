import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { WeakPasswordListForm, WeakPasswordSaveForm, WeakPasswordVO } from './types';

export const weakPasswordApi = {
  listPage: (form: WeakPasswordListForm) =>
    request
      .post<Result<PageData<WeakPasswordVO>>>('/sys/base/weak-password/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<WeakPasswordVO>>('/sys/base/weak-password/detail', { id })
      .then((response) => response.data.data),
  save: (form: WeakPasswordSaveForm) =>
    request
      .post<Result<string>>('/sys/base/weak-password/save', form)
      .then((response) => response.data.data),
  delete: (form: { id: string; version: number }) =>
    request
      .post<Result<string>>('/sys/base/weak-password/delete', form)
      .then((response) => response.data.data),
};
