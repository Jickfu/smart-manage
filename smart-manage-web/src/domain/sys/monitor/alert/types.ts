import type { PageData } from '@/types/api';
export interface UserRef extends Record<string, unknown> {
  id: string;
  number: string;
  name: string;
}
export interface AlertRule {
  id: string;
  ruleCode: string;
  name: string;
  scopeType: 'HOST' | 'INSTANCE';
  enabled: boolean;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  threshold: number;
  durationSeconds: number;
  recoveryThreshold?: number;
  repeatIntervalSeconds: number;
  emailEnabled: boolean;
  description?: string;
  version: number;
  valueKind: 'RATIO' | 'COUNT' | 'BOOLEAN' | 'RATE' | 'DURATION_MS';
  displayUnit: string;
  minValue: number;
  maxValue?: number;
  recommendedThreshold: number;
  recipientUsers: UserRef[];
}
export interface AlertIncident {
  id: string;
  ruleCode: string;
  ruleName: string;
  severity: string;
  scopeType: string;
  scopeId: string;
  status: string;
  closeReason?: string;
  startedAt: string;
  firedAt?: string;
  recoveredAt?: string;
  lastValue?: number;
  peakValue?: number;
  lastValueDisplay: string;
  peakValueDisplay: string;
  summary: string;
}
export interface AlertRuleSave {
  id: string;
  version: number;
  enabled: boolean;
  severity: string;
  threshold: number;
  durationSeconds: number;
  recoveryThreshold?: number;
  repeatIntervalSeconds: number;
  emailEnabled: boolean;
  recipientUserIds: string[];
  description?: string;
}
export type AlertIncidentPage = PageData<AlertIncident>;
