import type { PageForm } from '@/types/api';

export interface SysParamListForm extends PageForm {
  appId?: string;
  cloudId?: string;
  globalOnly?: boolean;
  keyword?: string;
}

export interface SysParamVO {
  id: string;
  version: number;
  number: string;
  name: string;
  value?: string;
  description?: string;
  isSystem: boolean;
  appId?: string;
  appName?: string;
}

export interface SysParamSaveForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  value?: string;
  description?: string;
  appId?: string;
}
