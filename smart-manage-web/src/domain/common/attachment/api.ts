import request from '@/api/request';
import type { Result } from '@/types/api';
import type { AttachmentDownloadAccess, BusinessAttachment } from './types';

const uploadSessionHeaders = (uploadSessionId?: string) =>
  uploadSessionId ? { 'X-Upload-Session': uploadSessionId } : undefined;

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
      >('/sys/base/attachment/delete', { id }, { headers: uploadSessionHeaders(uploadSessionId) })
      .then((response) => response.data.data),
  downloadAccess: (id: string, uploadSessionId?: string) =>
    request
      .post<
        Result<AttachmentDownloadAccess>
      >('/sys/base/attachment/downloadAccess', { id }, { headers: uploadSessionHeaders(uploadSessionId) })
      .then((response) => response.data.data),
  download: (id: string, uploadSessionId?: string) =>
    request
      .post<Blob>(
        '/sys/base/attachment/download',
        { id },
        {
          headers: uploadSessionHeaders(uploadSessionId),
          responseType: 'blob',
        },
      )
      .then((response) => response.data),
};
