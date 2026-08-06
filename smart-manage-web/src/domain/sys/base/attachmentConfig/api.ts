import request from '@/api/request';
import type { Result } from '@/types/api';
import type { AttachmentConfigDetail, AttachmentConfigSaveForm } from './types';

export const attachmentConfigApi = {
  singleton: () =>
    request
      .get<Result<AttachmentConfigDetail>>('/sys/base/attachment-config/singleton')
      .then((response) => response.data.data),
  save: (form: AttachmentConfigSaveForm) =>
    request
      .post<Result<string>>('/sys/base/attachment-config/save', form)
      .then((response) => response.data.data),
};
