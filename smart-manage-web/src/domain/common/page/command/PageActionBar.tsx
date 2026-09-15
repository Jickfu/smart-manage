import type { PermissionAction } from '../access/access';
import { PermissionActions } from '../access/PermissionActions';
import { resolvePageActions, type PageAction } from './pageActions';

/** 组合布局与统一交互冻结；业务处理函数仅作为按钮事件传递。 */
export function PageActionBar<TBuiltin extends string>({
  prefix,
  declaration,
  builtins,
  disabled = false,
}: {
  prefix?: string;
  declaration?: readonly PageAction<TBuiltin>[];
  builtins: readonly PermissionAction[];
  disabled?: boolean;
}) {
  return (
    <PermissionActions
      prefix={prefix}
      actions={resolvePageActions(declaration, builtins).map((action) => ({
        ...action,
        disabled: disabled || action.disabled,
      }))}
    />
  );
}
