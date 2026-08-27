import request from '@/api/request';
import type { Result } from '@/types/api';

export interface ActiveUiConfig {
  pageTitle?: string;
  systemName?: string;
  headerLogo?: string | null;
  watermarkEnabled?: boolean;
  watermarkContent?: string | null;
  watermarkShowName?: boolean;
  watermarkShowPhone?: boolean;
  watermarkShowEmail?: boolean;
  watermarkShowNumber?: boolean;
  watermarkShowRootOrg?: boolean;
  watermarkGapX?: number;
  watermarkGapY?: number;
  watermarkFontSize?: number;
}

export const activeUiConfigQueryKey = ['sys', 'ui-config', 'active'] as const;

export const getActiveUiConfig = () =>
  request
    .post<Result<ActiveUiConfig>>('/sys/base/ui-config/active', {})
    .then((response) => response.data.data);
