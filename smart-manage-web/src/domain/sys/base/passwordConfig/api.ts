import request from '@/api/request';
import type { Result } from '@/types/api';
import type { PasswordConfigDetail, PasswordConfigSaveForm } from './types';

export const passwordConfigApi = {
  singleton: () =>
    request
      .get<Result<PasswordConfigDetail>>('/sys/base/password-config/singleton')
      .then((response) => response.data.data),
  save: (form: PasswordConfigSaveForm) =>
    request
      .post<Result<string>>('/sys/base/password-config/save', form)
      .then((response) => response.data.data),
};
