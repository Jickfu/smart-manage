import { Button, Space } from 'antd';
import type { PermissionAction } from './access';
import { usePermissionAccess } from './usePermissionAccess';

interface PermissionActionsProps {
  prefix?: string;
  /** 弹框页脚关闭分组，由 AppModal 统一控制按钮间距。 */
  grouped?: boolean;
  actions: PermissionAction[];
}

/** 统一渲染页面命令，并按后端返回的当前用户权限过滤。 */
export function PermissionActions({ prefix, actions, grouped = true }: PermissionActionsProps) {
  const { can } = usePermissionAccess(prefix);
  const visibleActions = actions.filter((action) => can(action.permission));
  const buttons = visibleActions.map((action) => (
    <Button
      key={action.key}
      type={action.type ?? (action.danger ? 'default' : 'primary')}
      danger={action.danger}
      disabled={action.disabled}
      loading={action.loading}
      onClick={action.onClick}
    >
      {action.label}
    </Button>
  ));
  return grouped ? <Space>{buttons}</Space> : <>{buttons}</>;
}
