import type { PageForm } from '@/types/api';

/** 权限列表查询 */
export interface PermissionListForm extends PageForm {
  keyword?: string;
}

/** 权限列表项 */
export interface PermissionListVO {
  id: string;
  name: string;
  number: string;
  appId: string;
}

/** 权限全量查询（不分页） */
export interface PermissionListAllVO {
  id: string;
  name: string;
  number: string;
  appId: string;
}

/** 权限详情 */
export interface PermissionDetailVO {
  id: string;
  version: number;
  name: string;
  number: string;
  appId: string;
  createTime?: string;
  updateTime?: string;
}

/** 权限保存 — ID 均以字符串传递 */
export interface PermissionSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  appId: string;
}

/** 权限选择器列表项 */
export interface PermissionSelectVO {
  id: string;
  number: string;
  name: string;
  appId: string;
}
