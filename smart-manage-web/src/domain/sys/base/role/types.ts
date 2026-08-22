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
  description: string;
}

/** 角色详情 */
export interface RoleDetailVO {
  id: string;
  name: string;
  number: string;
  description?: string;
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
  description?: string;
}

/** 角色选择器列表项 */
export interface RoleSelectVO {
  id: string;
  number: string;
  name: string;
  description?: string;
}
