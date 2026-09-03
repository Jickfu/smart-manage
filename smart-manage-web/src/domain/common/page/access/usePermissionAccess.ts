import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getCurrentPermissions } from '@/api/user';

/** 页面按钮权限判定，权限数据由 TanStack Query 按前缀共享。 */
export function usePermissionAccess(prefix?: string) {
  const query = useQuery({
    queryKey: ['current-user-permissions', prefix],
    queryFn: () => getCurrentPermissions(prefix!),
    enabled: Boolean(prefix),
    staleTime: 5 * 60 * 1000,
  });
  // 权限查询失败时拒绝继续使用旧授权；此规则比普通查询保留缓存更严格。
  const permissionSet = useMemo(
    () => new Set(query.error ? [] : (query.data ?? [])),
    [query.data, query.error],
  );
  return {
    can: (permission?: string) =>
      !permission || permissionSet.has('*') || permissionSet.has(permission),
    loading: query.isLoading,
  };
}
