import request from '@/api/request';
import type { Result } from '@/types/api';
import type { BusinessAttachment } from './types';

export const businessAttachmentApi = {
  upload: (resourceType: string, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('bizType', resourceType);
    return request
      .post<Result<BusinessAttachment>>('/sys/base/attachment/upload', formData)
      .then((response) => response.data.data);
  },
  delete: (id: string, uploadSessionId?: string) =>
    request
      .post<
        Result<string>
      >('/sys/base/attachment/delete', { id }, { headers: uploadSessionId ? { 'X-Upload-Session': uploadSessionId } : undefined })
      .then((response) => response.data.data),
};
