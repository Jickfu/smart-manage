import type { PageForm } from '@/types/api';

export interface BasicDataTreeNode {
  key: string;
  type: 'cloud' | 'category';
  id: string;
  name: string;
  enabled: boolean;
  children: BasicDataTreeNode[];
}

export interface BasicDataCategory {
  id: string;
  cloudId: string;
  cloudName?: string;
  number: string;
  name: string;
  remark?: string;
  enabled: boolean;
  systemPreset: boolean;
  version: number;
}

export interface BasicDataCategorySaveForm {
  id?: string;
  version?: number;
  cloudId: string;
  number: string;
  name: string;
  remark?: string;
  enabled: boolean;
}

export interface BasicDataListForm extends PageForm {
  categoryId?: string;
  keyword?: string;
}

export interface BasicDataListVO {
  id: string;
  categoryId: string;
  categoryName: string;
  parentId?: string;
  number: string;
  name: string;
  remark?: string;
  sort: number;
  enabled: boolean;
  systemPreset: boolean;
  level: number;
  numberPath: string;
  namePath: string;
  isLeaf: boolean;
  version: number;
  updateTime?: string;
}

export type BasicDataItemDetailVO = BasicDataListVO;

export interface BasicDataOption {
  id: string;
  parentId?: string;
  number: string;
  name: string;
  namePath: string;
  isLeaf: boolean;
}

export interface BasicDataSaveForm {
  id?: string;
  version?: number;
  categoryId: string;
  parentId?: string;
  number: string;
  name: string;
  remark?: string;
  sort: number;
  enabled: boolean;
}
