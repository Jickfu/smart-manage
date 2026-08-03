import request from '@/api/request';
import type { PageData, Result, MenuVO } from '@/types/api';
import type {
  MenuDetailVO,
  MenuListForm,
  MenuListVO,
  MenuSaveForm,
  MenuSelectVO,
  MenuTreeVO,
} from './types';

/** 根据应用 number 获取当前用户菜单树 */
export function getUserMenusByAppNumber(number: string) {
  return request
    .post<Result<MenuVO>>('/sys/base/menu/getUserMenusByAppNumber', { number })
    .then((res) => res.data.data);
}

/** 根据应用 id 获取当前用户菜单树 */
export function getUserMenusByAppId(id: string) {
  return request
    .post<Result<MenuVO>>('/sys/base/menu/getUserMenusByAppId', { id })
    .then((res) => res.data.data);
}

export const menuApi = {
  listPage: (form: MenuListForm) =>
    request
      .post<Result<PageData<MenuListVO>>>('/sys/base/menu/listPage', form)
      .then((res) => res.data.data),

  listByApp: (appId: string) =>
    request
      .post<Result<MenuTreeVO[]>>('/sys/base/menu/listByApp', { appId })
      .then((res) => res.data.data),

  select: (form: { pageNum: number; pageSize: number; keyword?: string; excludeId?: string }) =>
    request
      .post<Result<PageData<MenuSelectVO>>>('/sys/base/menu/select', form)
      .then((res) => res.data.data),

  detail: (id: string) =>
    request
      .post<Result<MenuDetailVO>>('/sys/base/menu/detail', { id })
      .then((res) => res.data.data),

  save: (form: MenuSaveForm) =>
    request.post<Result<string>>('/sys/base/menu/save', form).then((res) => res.data.data),

  delete: (id: string) =>
    request.post<Result<string>>('/sys/base/menu/delete', { id }).then((res) => res.data.data),

  setEnabled: (ids: string[], enabled: boolean) =>
    request
      .post<Result<string>>(enabled ? '/sys/base/menu/enable' : '/sys/base/menu/disable', { ids })
      .then((res) => res.data.data),
};
