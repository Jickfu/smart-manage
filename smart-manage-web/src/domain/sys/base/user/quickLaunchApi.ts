import request from '@/api/request';
import type { Result } from '@/types/api';

export type HomeQuickLaunchScope = 'SYSTEM' | 'APPLICATION';

export interface QuickLaunchScopeForm {
  scope: HomeQuickLaunchScope;
  appNumber?: string;
}

export interface QuickLaunchItemVO {
  menuId: string;
  menuNumber: string;
  name: string;
  icon?: string;
  appNumber: string;
  appName: string;
  groupName?: string;
  component?: string;
  targetType?: 'INTERNAL_PAGE' | 'EXTERNAL_LINK';
  externalUrl?: string;
  externalOpenMode?: 'NEW_TAB' | 'IFRAME';
}

export interface QuickLaunchConfigurationVO {
  options: QuickLaunchItemVO[];
  selectedMenuIds: string[];
}

export const quickLaunchQueryKeys = {
  all: ['sys', 'base', 'user', 'home-quick-launch'] as const,
  list: (form: QuickLaunchScopeForm) => [...quickLaunchQueryKeys.all, 'list', form] as const,
  configuration: (form: QuickLaunchScopeForm) =>
    [...quickLaunchQueryKeys.all, 'configuration', form] as const,
};

export const quickLaunchApi = {
  list: (form: QuickLaunchScopeForm) =>
    request
      .post<Result<QuickLaunchItemVO[]>>('/sys/base/user/current/home-quick-launch/list', form)
      .then((response) => response.data.data),
  configuration: (form: QuickLaunchScopeForm) =>
    request
      .post<
        Result<QuickLaunchConfigurationVO>
      >('/sys/base/user/current/home-quick-launch/configuration', form)
      .then((response) => response.data.data),
  save: (form: QuickLaunchScopeForm & { menuIds: string[] }) =>
    request
      .post<Result<void>>('/sys/base/user/current/home-quick-launch/save', form)
      .then((response) => response.data),
};
