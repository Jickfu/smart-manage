import type { PageForm } from '@/types/api';

export interface DomainListForm extends PageForm {
  keyword?: string;
  enabled?: boolean;
}

export interface DomainListVO {
  id: string;
  name: string;
  number: string;
  seq: number;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 领域详情 */
export interface DomainDetailVO {
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

/** 领域选择器查询参数 */
export interface DomainSelectForm extends PageForm {
  keyword?: string;
  enabled?: boolean;
}

/** 领域选择器列表项 */
export interface DomainSelectVO {
  id: string;
  name: string;
  number: string;
  enabled: boolean;
}

/** 领域保存 */
export interface DomainSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  seq: number;
}
