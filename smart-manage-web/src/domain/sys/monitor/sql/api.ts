import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  SqlCatalogDetail,
  SqlCatalogObject,
  SqlCatalogResult,
  SqlExecutionResult,
  SqlLogDetail,
  SqlLogListForm,
  SqlLogListItem,
} from './types';

export const sqlApi = {
  execute: (sql: string) =>
    request
      .post<Result<SqlExecutionResult>>('/sys/monitor/sql/execute', { sql })
      .then((response) => response.data.data),
  catalog: (keyword?: string) =>
    request
      .post<Result<SqlCatalogResult>>('/sys/monitor/sql/catalog/list', { keyword })
      .then((response) => response.data.data),
  catalogDetail: (object: Pick<SqlCatalogObject, 'schemaName' | 'objectName'>) =>
    request
      .post<Result<SqlCatalogDetail>>('/sys/monitor/sql/catalog/detail', object)
      .then((response) => response.data.data),
  listPage: (form: SqlLogListForm) =>
    request
      .post<Result<PageData<SqlLogListItem>>>('/sys/monitor/sql/log/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<SqlLogDetail>>('/sys/monitor/sql/log/detail', { id })
      .then((response) => response.data.data),
};
