import request from '@/api/request';
import type { Result } from '@/types/api';
import type { AttachmentVO, UiConfigDetail } from './types';

export const uiConfigApi = {
  singleton: () =>
    request
      .get<Result<UiConfigDetail>>('/sys/base/ui-config/singleton')
      .then((response) => response.data.data),
  save: (form: UiConfigDetail) =>
    request
      .post<Result<string>>('/sys/base/ui-config/save', form)
      .then((response) => response.data.data),
  uploadImage: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bizType', 'SYS_UI_CONFIG');
    return request
      .post<Result<AttachmentVO>>('/sys/base/attachment/upload', formData)
      .then((response) => response.data.data);
  },
  deleteAttachment: (id: string) =>
    request
      .post<Result<string>>('/sys/base/attachment/delete', { id })
      .then((response) => response.data.data),
};
