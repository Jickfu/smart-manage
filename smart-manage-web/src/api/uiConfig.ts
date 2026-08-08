import request from '@/api/request';
import type { Result } from '@/types/api';

export interface ActiveUiConfig {
  pageTitle?: string;
  systemName?: string;
  headerLogo?: string | null;
}

export const activeUiConfigQueryKey = ['sys', 'ui-config', 'active'] as const;

export const getActiveUiConfig = () =>
  request
    .post<Result<ActiveUiConfig>>('/sys/base/ui-config/active', {})
    .then((response) => response.data.data);
