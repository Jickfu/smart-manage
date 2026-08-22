import type { PageForm } from '@/types/api';
import type { ReferenceVO } from '@/domain/sys/base/common/types';

export interface SysParamListForm extends PageForm {
  featureId?: string;
  appId?: string;
  domainId?: string;
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
  featureId?: string;
  featureName?: string;
  appId?: string;
  appName?: string;
}

export interface SysParamDetailVO extends Omit<
  SysParamVO,
  'featureId' | 'featureName' | 'appId' | 'appName'
> {
  feature?: ReferenceVO;
}

export interface SysParamSaveForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  value?: string;
  description?: string;
  featureId?: string;
}
