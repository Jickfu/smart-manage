import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type {
  ScriptDetail,
  ScriptExecutionForm,
  ScriptExecutionResult,
  ScriptListForm,
  ScriptListItem,
  ScriptLogDetail,
  ScriptLogListForm,
  ScriptLogListItem,
  ScriptSaveForm,
  ScriptApiService,
} from './types';

export const scriptApi = {
  apiMetadata: () =>
    request
      .get<Result<ScriptApiService[]>>('/sys/monitor/script/apiMetadata')
      .then((response) => response.data.data),
  execute: (form: ScriptExecutionForm) =>
    request
      .post<Result<ScriptExecutionResult>>('/sys/monitor/script/execute', form)
      .then((response) => response.data.data),
  listPage: (form: ScriptListForm) =>
    request
      .post<Result<PageData<ScriptListItem>>>('/sys/monitor/script/listPage', form)
      .then((response) => response.data.data),
  detail: (id: string) =>
    request
      .post<Result<ScriptDetail>>('/sys/monitor/script/detail', { id })
      .then((response) => response.data.data),
  createNewData: () =>
    request
      .get<Result<Partial<ScriptDetail>>>('/sys/monitor/script/createNewData')
      .then((response) => response.data.data),
  save: (form: ScriptSaveForm) =>
    request
      .post<Result<string>>('/sys/monitor/script/save', form)
      .then((response) => response.data.data),
  delete: (id: string, version: number) =>
    request
      .post<Result<string>>('/sys/monitor/script/delete', { id, version })
      .then((response) => response.data.data),
  logListPage: (form: ScriptLogListForm) =>
    request
      .post<Result<PageData<ScriptLogListItem>>>('/sys/monitor/script/log/listPage', form)
      .then((response) => response.data.data),
  logDetail: (id: string) =>
    request
      .post<Result<ScriptLogDetail>>('/sys/monitor/script/log/detail', { id })
      .then((response) => response.data.data),
};
