import type { PageForm } from '@/types/api';

/** 菜单列表查询 */
export interface MenuListForm extends PageForm {
  appId?: string;
  parentId?: string;
  keyword?: string;
}

/** 菜单列表项 — 所有 ID 均为字符串 */
export interface MenuListVO {
  id: string;
  number: string;
  level: number;
  parentId: string;
  name: string;
  path: string;
  component: string;
  sort: number;
  icon: string;
}

/** 菜单树节点（全量，不分页） */
export interface MenuTreeVO {
  id: string;
  number: string;
  name: string;
  level: number;
  parentId: string;
  path: string;
  component: string;
  sort: number;
  icon: string;
  enabled: boolean;
}

/** 菜单详情 */
export interface MenuDetailVO {
  id: string;
  number: string;
  name: string;
  level: number;
  appId: string;
  permissionId: string;
  path: string;
  component: string;
  icon: string;
  description: string;
  sort: number;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
  parent?: {
    id: string;
    number: string;
    name: string;
  };
}

/** 菜单保存 — ID 均以字符串传递 */
export interface MenuSaveForm {
  id?: string;
  number?: string;
  name: string;
  level: number;
  parentId?: string;
  appId: string;
  permissionId?: string;
  path?: string;
  component?: string;
  icon?: string;
  description?: string;
  sort?: number;
}

/** 菜单选择器列表项 */
export interface MenuSelectVO {
  id: string;
  number: string;
  name: string;
  level: number;
  enabled: boolean;
}
