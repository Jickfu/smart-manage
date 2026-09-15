import type { PermissionAction } from '../access/access';

/** 内置引用只决定位置，不能覆盖底座维护的权限、状态与命令处理。 */
export type PageAction<TBuiltin extends string> =
  | { builtin: TBuiltin; key?: never }
  | (PermissionAction & { builtin?: never });

/** 显式声明完整替代默认布局；暂不可用的内置命令跳过，其他按钮保持相对顺序。 */
export function resolvePageActions<TBuiltin extends string>(
  declaration: readonly PageAction<TBuiltin>[] | undefined,
  builtins: readonly PermissionAction[],
): PermissionAction[] {
  if (declaration === undefined) {
    return [...builtins].sort(
      (leftAction, rightAction) =>
        Number(Boolean(leftAction.danger)) - Number(Boolean(rightAction.danger)),
    );
  }
  const builtinMap = new Map(builtins.map((action) => [action.key, action]));
  const identities = new Set<string>();
  return declaration.flatMap((entry) => {
    // 命名空间隔离业务 key 与内置 key；重复声明属于配置错误，不能静默丢弃。
    const identity =
      entry.builtin === undefined ? `custom:${entry.key}` : `builtin:${entry.builtin}`;
    if (identities.has(identity)) throw new Error(`重复的页面操作声明：${identity}`);
    identities.add(identity);
    const action = 'label' in entry ? entry : builtinMap.get(entry.builtin);
    return action ? [{ ...action, key: identity }] : [];
  });
}
