import type { PageForm } from '@/types/api';

/** 应用 VO */
export interface AppVO {
  id: string;
  cloudNumber: string;
  number: string;
  name: string;
  icon: string;
  iconColor: string;
  seq: number;
  description: string;
}

/** 按领域分组的应用列表 */
export interface CloudAppsVO {
  id: string;
  name: string;
  number: string;
  seq: number;
  appList: AppVO[];
}

/** 应用列表查询 */
export interface AppListForm extends PageForm {
  cloudId?: string;
  keyword?: string;
}

/** 应用列表项 */
export interface AppListVO {
  id: string;
  name: string;
  number: string;
  icon: string;
  iconColor: string;
  seq: number;
  description: string;
  cloudId: string;
  cloudName: string;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 应用详情 */
export interface AppDetailVO {
  id: string;
  version: number;
  name: string;
  number: string;
  icon: string;
  iconColor: string;
  seq: number;
  description: string;
  cloud: {
    id: string;
    number: string;
    name: string;
  };
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

/** 应用保存 */
export interface AppSaveForm {
  id?: string;
  version?: number;
  name: string;
  number: string;
  icon?: string;
  iconColor?: string;
  seq?: number;
  description?: string;
  cloudId: string;
}
