import type { PageData } from '@/types/api';
export interface UserRef extends Record<string, unknown> {
  id: string;
  number: string;
  name: string;
}
export interface AlertRule {
  id: string;
  rule_code: string;
  name: string;
  scope_type: 'HOST' | 'INSTANCE';
  enabled: boolean;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  threshold: number;
  duration_seconds: number;
  recovery_threshold?: number;
  repeat_interval_seconds: number;
  email_enabled: boolean;
  description?: string;
  version: number;
  recipient_users: UserRef[];
}
export interface AlertIncident {
  id: string;
  rule_code: string;
  rule_name: string;
  severity: string;
  scope_type: string;
  scope_id: string;
  status: string;
  started_at: string;
  fired_at?: string;
  recovered_at?: string;
  last_value?: number;
  peak_value?: number;
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
