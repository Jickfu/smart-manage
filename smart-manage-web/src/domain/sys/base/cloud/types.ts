import type { PageForm } from '@/types/api';

export interface CloudListForm extends PageForm {
  keyword?: string;
  enabled?: boolean;
}

export interface CloudListVO {
  id: string;
  name: string;
  number: string;
  seq: number;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 云详情 */
export interface CloudDetailVO {
  id: string;
  version: number;
  name: string;
  number: string;
  seq: number;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
  createUser?: string;
  updateUser?: string;
}

/** 云选择器查询参数 */
export interface CloudSelectForm extends PageForm {
  keyword?: string;
  enabled?: boolean;
}

/** 云选择器列表项 */
export interface CloudSelectVO {
  id: string;
  name: string;
  number: string;
  enabled: boolean;
}

/** 云保存 */
export interface CloudSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  seq: number;
}
