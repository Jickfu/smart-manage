import request from '@/api/request';
import type { Result } from '@/types/api';
import type { FileConfigDetail, FileConfigSaveForm } from './types';

export const fileConfigApi = {
  singleton: () =>
    request
      .get<Result<FileConfigDetail>>('/sys/base/file-config/singleton')
      .then((response) => response.data.data),
  save: (form: FileConfigSaveForm) =>
    request
      .post<Result<string>>('/sys/base/file-config/save', form)
      .then((response) => response.data.data),
  testFtp: (form: Omit<FileConfigSaveForm, 'id' | 'version' | 'storageType' | 'localDir'>) =>
    request
      .post<Result<string>>('/sys/base/file-config/test-ftp', form)
      .then((response) => response.data.data),
};
