import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  BasicDataCreateNewDataVO,
  BasicDataDetailVO,
  BasicDataListForm,
  BasicDataListVO,
  BasicDataSaveForm,
} from './types';

export const basicDataApi = {
  listPage: (form: BasicDataListForm) =>
    request
      .post<Result<PageData<BasicDataListVO>>>('/sys/base/basic-data/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<BasicDataDetailVO>>('/sys/base/basic-data/detail', { id })
      .then((response) => response.data.data),
  createNewData: () =>
    request
      .get<Result<BasicDataCreateNewDataVO>>('/sys/base/basic-data/createNewData')
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
