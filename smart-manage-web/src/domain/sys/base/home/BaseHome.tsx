import { Button, Card } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorDescription } from '@/domain/common/component/RequestErrorState';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';
import { serializeListFilters } from '@/domain/common/page/list/listQuery';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import { userApi } from '../user/api';
import { userAccess } from '../user/permissions';
import { userQueryKeys } from '../user/queryKeys';
import { roleApi } from '../role/api';
import { roleAccess } from '../role/permissions';
import { roleQueryKeys } from '../role/queryKeys';
import { orgApi } from '../org/api';
import { orgAccess } from '../org/permissions';
import { orgQueryKeys } from '../org/queryKeys';
import type { OrgTreeNode } from '../org/types';
import './BaseHome.css';

const enabledUsersForm = {
  pageNum: 1,
  pageSize: 1,
  filters: serializeListFilters([
    { field: 'enabled', type: 'boolean', operator: 'EQ', value: true },
  ]),
};
const rolesForm = { pageNum: 1, pageSize: 1 };
function countOrganizations(nodes: OrgTreeNode[]): number {
  return nodes.reduce((total, node) => total + 1 + countOrganizations(node.children), 0);
}
function Metric({
  allowed,
  loading,
  value,
  error,
  onRetry,
}: {
  allowed: boolean;
  loading: boolean;
  value?: number;
  error: Error | null;
  onRetry: () => void;
}) {
  // 只有真实数字使用大字号，加载、权限和失败状态保持正文密度。
  if (loading) return <div className="sm-base-home-metric-state">加载中…</div>;
  if (!allowed) return <div className="sm-base-home-metric-state">无查看权限</div>;
  if (error)
    return (
      <div className="sm-base-home-metric-state" role="alert">
        <div>加载失败</div>
        <div>
          <RequestErrorDescription error={error} />
        </div>
        <Button type="primary" onClick={onRetry}>
          重试
        </Button>
      </div>
    );
  if (value === undefined) return <div className="sm-base-home-metric-state">—</div>;
  return <strong className="sm-app-home-metric-value">{value.toLocaleString('zh-CN')}</strong>;
}
const BaseHome = () => {
  const access = usePermissionAccess('sys:base');
  const canViewUsers = access.can(userAccess.permissions.list);
  const canViewOrgs = access.can(orgAccess.permissions.listPage);
  const canViewRoles = access.can(roleAccess.permissions.list);
  // 复用所属模块查询键，维护数据后沿用原有缓存失效。
  const users = useQuery({
    queryKey: userQueryKeys.list(enabledUsersForm),
    queryFn: () => userApi.listPage(enabledUsersForm),
    enabled: canViewUsers,
    meta: { errorPresentation: 'local-initial' },
  });
  const organizations = useQuery({
    queryKey: orgQueryKeys.tree(false),
    queryFn: () => orgApi.tree(false),
    enabled: canViewOrgs,
    meta: { errorPresentation: 'local-initial' },
  });
  const roles = useQuery({
    queryKey: roleQueryKeys.list(rolesForm),
    queryFn: () => roleApi.listPage(rolesForm),
    enabled: canViewRoles,
    meta: { errorPresentation: 'local-initial' },
  });
  return (
    <div className="sm-app-home">
      <QuickLaunchCard scope="APPLICATION" appNumber="base" />
      <HomeCardGrid>
        <Card className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-1">
          <div className="sm-app-home-metric-title">启用用户</div>
          <Metric
            allowed={canViewUsers}
            loading={access.loading || users.isLoading}
            value={users.data?.total}
            error={getBlockingQueryError(users)}
            onRetry={() => {
              if (canViewUsers) void users.refetch();
            }}
          />
          <div className="sm-app-home-metric-caption">启用状态的用户数量</div>
        </Card>
        <Card className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-3">
          <div className="sm-app-home-metric-title">组织与授权</div>
          <div className="sm-app-home-metric-pairs">
            <div>
              <Metric
                allowed={canViewOrgs}
                loading={access.loading || organizations.isLoading}
                value={organizations.data && countOrganizations(organizations.data)}
                error={getBlockingQueryError(organizations)}
                onRetry={() => {
                  if (canViewOrgs) void organizations.refetch();
                }}
              />
              <span>未封存组织</span>
            </div>
            <div>
              <Metric
                allowed={canViewRoles}
                loading={access.loading || roles.isLoading}
                value={roles.data?.total}
                error={getBlockingQueryError(roles)}
                onRetry={() => {
                  if (canViewRoles) void roles.refetch();
                }}
              />
              <span>已定义角色</span>
            </div>
          </div>
        </Card>
      </HomeCardGrid>
    </div>
  );
};
export default BaseHome;
