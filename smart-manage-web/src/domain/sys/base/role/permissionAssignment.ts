import type { DataNode } from 'antd/es/tree';
import type { PermissionListAllVO } from '@/domain/sys/base/permission/types';

export type PermissionAssignmentScope =
  | { type: 'all' }
  | { type: 'domain'; domainId: string }
  | { type: 'app'; appId: string }
  | { type: 'feature'; featureId: string }
  | { type: 'app-level'; appId: string };

interface PermissionFeatureGroup {
  id: string;
  name: string;
  permissions: PermissionListAllVO[];
}

interface PermissionAppGroup {
  id: string;
  name: string;
  permissions: PermissionListAllVO[];
  appLevelPermissions: PermissionListAllVO[];
  features: Map<string, PermissionFeatureGroup>;
}

interface PermissionDomainGroup {
  id: string;
  name: string;
  permissions: PermissionListAllVO[];
  apps: Map<string, PermissionAppGroup>;
}

const countTitle = (
  label: string,
  permissions: readonly PermissionListAllVO[],
  selectedIdSet: ReadonlySet<string>,
) =>
  `${label}（${permissions.filter((permission) => selectedIdSet.has(permission.id)).length}/${permissions.length}）`;

export const permissionAssignmentScopeKey = (scope: PermissionAssignmentScope): string => {
  if (scope.type === 'all') return 'all';
  if (scope.type === 'domain') return `domain:${scope.domainId}`;
  if (scope.type === 'feature') return `feature:${scope.featureId}`;
  return `${scope.type}:${scope.appId}`;
};

export const parsePermissionAssignmentScope = (key: React.Key): PermissionAssignmentScope => {
  const [type, id] = String(key).split(':');
  if (type === 'domain' && id) return { type: 'domain', domainId: id };
  if (type === 'app' && id) return { type: 'app', appId: id };
  if (type === 'feature' && id) return { type: 'feature', featureId: id };
  if (type === 'app-level' && id) return { type: 'app-level', appId: id };
  return { type: 'all' };
};

/** 按真实归属的领域、应用、功能身份组织导航，各层只承担筛选和统计语义。 */
export const buildPermissionAssignmentTree = (
  permissions: readonly PermissionListAllVO[],
  selectedIdSet: ReadonlySet<string>,
): DataNode[] => {
  const domainGroups = new Map<string, PermissionDomainGroup>();
  for (const permission of permissions) {
    const domainGroup = domainGroups.get(permission.domainId) ?? {
      id: permission.domainId,
      name: permission.domainName,
      permissions: [],
      apps: new Map<string, PermissionAppGroup>(),
    };
    domainGroup.permissions.push(permission);
    const appGroup = domainGroup.apps.get(permission.appId) ?? {
      id: permission.appId,
      name: permission.appName,
      permissions: [],
      appLevelPermissions: [],
      features: new Map<string, PermissionFeatureGroup>(),
    };
    appGroup.permissions.push(permission);
    if (!permission.featureId) {
      appGroup.appLevelPermissions.push(permission);
    } else {
      const featureGroup = appGroup.features.get(permission.featureId) ?? {
        id: permission.featureId,
        name: permission.featureName ?? permission.featureKey ?? '未命名功能',
        permissions: [],
      };
      featureGroup.permissions.push(permission);
      appGroup.features.set(permission.featureId, featureGroup);
    }
    domainGroup.apps.set(permission.appId, appGroup);
    domainGroups.set(permission.domainId, domainGroup);
  }

  return [
    {
      key: 'all',
      title: countTitle('全部权限', permissions, selectedIdSet),
      children: [...domainGroups.values()].map((domainGroup) => ({
        key: `domain:${domainGroup.id}`,
        title: countTitle(domainGroup.name, domainGroup.permissions, selectedIdSet),
        children: [...domainGroup.apps.values()].map((appGroup) => ({
          key: `app:${appGroup.id}`,
          title: countTitle(appGroup.name, appGroup.permissions, selectedIdSet),
          children: [
            ...(appGroup.appLevelPermissions.length
              ? [
                  {
                    key: `app-level:${appGroup.id}`,
                    title: countTitle('应用级权限', appGroup.appLevelPermissions, selectedIdSet),
                    isLeaf: true,
                  },
                ]
              : []),
            ...[...appGroup.features.values()].map((featureGroup) => ({
              key: `feature:${featureGroup.id}`,
              title: countTitle(featureGroup.name, featureGroup.permissions, selectedIdSet),
              isLeaf: true,
            })),
          ],
        })),
      })),
    },
  ];
};

export const filterPermissionsByAssignmentScope = (
  permissions: readonly PermissionListAllVO[],
  scope: PermissionAssignmentScope,
): PermissionListAllVO[] => {
  if (scope.type === 'domain') {
    return permissions.filter((permission) => permission.domainId === scope.domainId);
  }
  if (scope.type === 'app') {
    return permissions.filter((permission) => permission.appId === scope.appId);
  }
  if (scope.type === 'feature') {
    return permissions.filter((permission) => permission.featureId === scope.featureId);
  }
  if (scope.type === 'app-level') {
    return permissions.filter(
      (permission) => permission.appId === scope.appId && !permission.featureId,
    );
  }
  return [...permissions];
};

export const getPermissionAssignmentScopeLabel = (
  permissions: readonly PermissionListAllVO[],
  scope: PermissionAssignmentScope,
): string => {
  if (scope.type === 'domain') {
    return (
      permissions.find((permission) => permission.domainId === scope.domainId)?.domainName ?? '领域'
    );
  }
  if (scope.type === 'app') {
    return permissions.find((permission) => permission.appId === scope.appId)?.appName ?? '应用';
  }
  if (scope.type === 'feature') {
    return (
      permissions.find((permission) => permission.featureId === scope.featureId)?.featureName ??
      '功能'
    );
  }
  if (scope.type === 'app-level') {
    const appName = permissions.find((permission) => permission.appId === scope.appId)?.appName;
    return `${appName ?? '应用'} / 应用级权限`;
  }
  return '全部权限';
};
