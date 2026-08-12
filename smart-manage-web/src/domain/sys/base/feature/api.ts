import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { FeatureListForm, FeatureSaveForm, FeatureVO } from './types';

export const featureApi = {
  listPage: (form: FeatureListForm) =>
    request
      .post<Result<PageData<FeatureVO>>>('/sys/base/feature/listPage', form)
      .then((response) => response.data.data),
  listAllVisible: () =>
    request
      .post<Result<FeatureVO[]>>('/sys/base/feature/listAllVisible', {})
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<FeatureVO>>('/sys/base/feature/detail', { id })
      .then((response) => response.data.data),
  save: (form: FeatureSaveForm) =>
    request
      .post<Result<string>>('/sys/base/feature/save', form)
      .then((response) => response.data.data),
};
