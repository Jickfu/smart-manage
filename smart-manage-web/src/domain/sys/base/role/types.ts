import type { PageForm } from '@/types/api';

/** 角色列表查询 */
export interface RoleListForm extends PageForm {
  keyword?: string;
}

/** 角色列表项 */
export interface RoleListVO {
  id: string;
  name: string;
  number: string;
  remark: string;
}

/** 角色详情 */
export interface RoleDetailVO {
  id: string;
  name: string;
  number: string;
  createTime?: string;
  updateTime?: string;
  version: number;
  permissionIds: string[];
}

/** 角色保存 */
export interface RoleSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  remark?: string;
}

/** 角色全量列表（不分页） */
export interface RoleListAllVO {
  id: string;
  number: string;
  name: string;
}

/** 角色选择器列表项 */
export interface RoleSelectVO {
  id: string;
  number: string;
  name: string;
}
