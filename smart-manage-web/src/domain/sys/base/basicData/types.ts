import type { PageForm } from '@/types/api';

export type BasicDataNumberMode = 'MANUAL' | 'AUTO_LOCKED' | 'AUTO_DEFAULT';

export interface BasicDataTreeNode {
  key: string;
  type: 'domain' | 'category';
  id: string;
  name: string;
  enabled: boolean;
  children: BasicDataTreeNode[];
}

export interface BasicDataCategory {
  id: string;
  domainId: string;
  domainName?: string;
  number: string;
  name: string;
  description?: string;
  enabled: boolean;
  systemPreset: boolean;
  version: number;
  numberMode: BasicDataNumberMode;
  numberRuleKey: string;
}

export interface BasicDataCategorySaveForm {
  id?: string;
  version?: number;
  domainId: string;
  number: string;
  name: string;
  description?: string;
  enabled: boolean;
  numberMode: BasicDataNumberMode;
  numberRuleKey: string;
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
  description?: string;
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
  number?: string;
  name: string;
  description?: string;
  sort: number;
}
