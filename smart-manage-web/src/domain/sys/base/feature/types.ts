import type { PageForm } from '@/types/api';

export interface FeatureListForm extends PageForm {
  keyword?: string;
  appId?: string;
  domainId?: string;
}

export interface FeatureVO {
  id: string;
  version: number;
  featureKey: string;
  appId: string;
  appName: string;
  domainId: string;
  domainName: string;
  defaultName: string;
  customName?: string;
  name: string;
  defaultSeq: number;
  customSeq?: number;
  seq: number;
  description?: string;
  visible: boolean;
  source: 'SYSTEM' | 'PLUGIN' | 'EXTERNAL';
}

export type FeatureDataObjectType =
  | 'TABLE'
  | 'PARTITIONED_TABLE'
  | 'VIEW'
  | 'MATERIALIZED_VIEW'
  | 'FOREIGN_TABLE';
export type FeatureDataObjectRole = 'PRIMARY' | 'DETAIL' | 'RELATION' | 'HISTORY' | 'LOG';

export interface FeatureDataObject {
  id: string;
  schemaName: string;
  objectName: string;
  objectType: FeatureDataObjectType;
  objectRole: FeatureDataObjectRole;
  displayName?: string;
  seq: number;
}

export interface FeatureSaveForm {
  id: string;
  version: number;
  customName?: string;
  customSeq?: number;
  description?: string;
  visible: boolean;
}
