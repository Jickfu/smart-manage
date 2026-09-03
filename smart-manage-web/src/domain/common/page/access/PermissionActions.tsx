import { Button, Space } from 'antd';
import type { PermissionAction } from './access';
import { usePermissionAccess } from './usePermissionAccess';

interface PermissionActionsProps {
  prefix?: string;
  actions: PermissionAction[];
}

/** 统一渲染页面命令，并按后端返回的当前用户权限过滤。 */
export function PermissionActions({ prefix, actions }: PermissionActionsProps) {
  const { can } = usePermissionAccess(prefix);
  const visibleActions = actions
    .filter((action) => can(action.permission))
    // 危险命令统一排在同组末尾，降低与常规命令混排造成的误触风险。
    .sort((leftAction, rightAction) => Number(leftAction.danger) - Number(rightAction.danger));
  return (
    <Space>
      {visibleActions.map((action) => (
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
      ))}
    </Space>
  );
}
