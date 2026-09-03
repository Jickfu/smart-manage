import type { ReactNode } from 'react';

export interface AccessResource<TPermissions> {
  prefix: string;
  permissions: TPermissions;
}

/** 页面命令的统一权限声明，适用于列表、编辑、分配和自定义页面。 */
export interface PermissionAction {
  key: string;
  label: ReactNode;
  permission?: string;
  onClick: () => void;
  type?: 'primary' | 'default';
  disabled?: boolean;
  loading?: boolean;
  danger?: boolean;
}

/** 集中生成领域权限码，业务页面只引用具名能力，不再拼接权限字符串。 */
export function defineAccessResource<TActions extends Record<string, string>>(
  prefix: string,
  actions: TActions,
): AccessResource<{ [TKey in keyof TActions]: string }> {
  return {
    prefix,
    permissions: Object.fromEntries(
      Object.entries(actions).map(([key, action]) => [key, `${prefix}:${action}`]),
    ) as { [TKey in keyof TActions]: string },
  };
}
