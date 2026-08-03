import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  BasicDataCategory,
  BasicDataCategorySaveForm,
  BasicDataItemDetailVO,
  BasicDataListForm,
  BasicDataListVO,
  BasicDataOption,
  BasicDataSaveForm,
  BasicDataTreeNode,
} from './types';

export const basicDataApi = {
  categoryTree: () =>
    request
      .get<Result<BasicDataTreeNode[]>>('/sys/base/basic-data/categoryTree')
      .then((response) => response.data.data),
  categoryDetail: (id: string) =>
    request
      .post<Result<BasicDataCategory>>('/sys/base/basic-data/categoryDetail', { id })
      .then((response) => response.data.data),
  saveCategory: (form: BasicDataCategorySaveForm) =>
    request
      .post<Result<string>>('/sys/base/basic-data/saveCategory', form)
      .then((response) => response.data.data),
  deleteCategory: (id: string, version: number) =>
    request
      .post<Result<string>>('/sys/base/basic-data/deleteCategory', { id, version })
      .then((response) => response.data.data),
  listPage: (form: BasicDataListForm) =>
    request
      .post<Result<PageData<BasicDataListVO>>>('/sys/base/basic-data/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<BasicDataItemDetailVO>>('/sys/base/basic-data/detail', { id })
      .then((response) => response.data.data),
  parentOptions: (categoryId: string, excludeId?: string) =>
    request
      .get<Result<BasicDataOption[]>>('/sys/base/basic-data/parentOptions', {
        params: { categoryId, excludeId },
      })
      .then((response) => response.data.data),
  save: (form: BasicDataSaveForm) =>
    request
      .post<Result<string>>('/sys/base/basic-data/save', form)
      .then((response) => response.data.data),
  delete: (id: string, version: number) =>
    request
      .post<Result<string>>('/sys/base/basic-data/delete', { id, version })
      .then((response) => response.data.data),
  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(
        enabled ? '/sys/base/basic-data/enable' : '/sys/base/basic-data/disable',
        {
          ids,
        },
      )
      .then((response) => response.data.data),
};
