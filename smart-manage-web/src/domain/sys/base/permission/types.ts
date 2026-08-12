import type { PageForm } from '@/types/api';

export interface PermissionListForm extends PageForm {
  keyword?: string;
  appId?: string;
  cloudId?: string;
  featureId?: string;
}

export interface PermissionListVO {
  id: string;
  name: string;
  number: string;
  appId: string;
  appName: string;
  featureId?: string;
  featureKey?: string;
  featureName?: string;
}

export type PermissionListAllVO = PermissionSelectVO;

export interface PermissionDetailVO extends PermissionListVO {
  version: number;
  createTime?: string;
  updateTime?: string;
}

export interface PermissionSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  featureId: string;
}

export interface PermissionSelectForm extends PageForm {
  keyword?: string;
  appId?: string;
  featureId?: string;
  appLevel?: boolean;
}

export interface PermissionSelectVO {
  id: string;
  number: string;
  name: string;
  appId: string;
  appName: string;
  featureId?: string;
  featureKey?: string;
  featureName?: string;
}
