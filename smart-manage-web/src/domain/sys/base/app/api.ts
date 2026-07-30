import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { AppVO, CloudAppsVO, AppListForm, AppListVO, AppDetailVO, AppSaveForm } from './types';

/** 获取当前用户可访问的应用列表（按领域分组） */
export function fetchApps() {
  return request.get<Result<CloudAppsVO[]>>('/sys/base/app/apps').then((res) => res.data.data);
}

/** 根据应用 number 打开应用详情 */
export function openByNumber(number: string) {
  return request
    .post<Result<AppVO>>('/sys/base/app/openByNumber', { number })
    .then((res) => res.data.data);
}

/** 全量云及应用（用于树选择） */
export function fetchAppsAll() {
  return request.get<Result<CloudAppsVO[]>>('/sys/base/app/appsAll').then((res) => res.data.data);
}

export const appApi = {
  listPage: (form: AppListForm) =>
    request
      .post<Result<PageData<AppListVO>>>('/sys/base/app/listPage', form)
      .then((res) => res.data.data),

  detail: (id: string) =>
    request.post<Result<AppDetailVO>>('/sys/base/app/detail', { id }).then((res) => res.data.data),

  save: (form: AppSaveForm) =>
    request.post<Result<string>>('/sys/base/app/save', form).then((res) => res.data.data),

  delete: (id: string) =>
    request.post<Result<string>>('/sys/base/app/delete', { id }).then((res) => res.data.data),

  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(enabled ? '/sys/base/app/enable' : '/sys/base/app/disable', { ids })
      .then((res) => res.data.data),
};
