import type { PageForm } from '@/types/api';

export interface FeatureListForm extends PageForm {
  keyword?: string;
  appId?: string;
  cloudId?: string;
}

export interface FeatureVO {
  id: string;
  version: number;
  featureKey: string;
  appId: string;
  appName: string;
  cloudId: string;
  cloudName: string;
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

export interface FeatureSaveForm {
  id: string;
  version: number;
  customName?: string;
  customSeq?: number;
  description?: string;
  visible: boolean;
}
