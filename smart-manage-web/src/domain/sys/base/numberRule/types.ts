import type { PageForm } from '@/types/api';

export type NumberScopeType = 'GLOBAL' | 'ORG' | 'CATEGORY';
export type NumberResetPeriod = 'NEVER' | 'YEAR' | 'MONTH' | 'DAY';
export type NumberSegmentType = 'FIXED' | 'VARIABLE' | 'DATE' | 'SEQUENCE';

export interface NumberRuleSegment {
  sort: number;
  segmentType: NumberSegmentType;
  value?: string;
  format?: string;
  length?: number;
  separator: string;
}

export interface NumberRuleListForm extends PageForm {
  keyword?: string;
  scopeType?: NumberScopeType;
  domainId?: string;
  appId?: string;
  featureId?: string;
  referenceKey?: string;
  enabled?: boolean;
}

export interface NumberRuleVO {
  id: string;
  version: number;
  ruleKey: string;
  referenceKey: string;
  referenceName: string;
  featureId: string;
  featureKey: string;
  featureName: string;
  appId: string;
  appName: string;
  domainId: string;
  domainName: string;
  name: string;
  pattern: string;
  scopeType: NumberScopeType;
  resetPeriod: NumberResetPeriod;
  startValue: number;
  enabled: boolean;
  systemPreset: boolean;
  defaultRule: boolean;
  usageCount: number;
  segments: NumberRuleSegment[];
  description?: string;
}

export interface NumberRuleSaveForm {
  id?: string;
  version?: number;
  ruleKey: string;
  referenceKey: string;
  name: string;
  scopeType: NumberScopeType;
  resetPeriod: NumberResetPeriod;
  startValue: number;
  segments: NumberRuleSegment[];
  description?: string;
}

export interface NumberRuleOption {
  id: string;
  ruleKey: string;
  referenceKey: string;
  name: string;
  scopeType: NumberScopeType;
  pattern: string;
  defaultRule: boolean;
}

export interface NumberVariable {
  key: string;
  name: string;
  segmentType: 'VARIABLE' | 'DATE';
}

export interface NumberReference {
  referenceKey: string;
  name: string;
  featureId: string;
  featureKey: string;
  featureName: string;
  appId: string;
  appName: string;
  domainId: string;
  domainName: string;
  allowedScopes: NumberScopeType[];
  variables: NumberVariable[];
}
