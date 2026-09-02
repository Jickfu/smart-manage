import type { DataScopeType, RoleDataScopeRule } from './types';

export interface DataScopeAssignmentState {
  defaultDataScope: DataScopeType;
  rules: readonly RoleDataScopeRule[];
}

/** 仅用于脏状态比较：组织视为集合，规则保留重复项，不改写显示顺序或提交载荷。 */
function comparisonSnapshot(state: DataScopeAssignmentState): string {
  const rules = state.rules.map((rule) => ({
    resourceType: rule.resourceType,
    action: rule.action ?? '',
    scopeType: rule.scopeType,
    orgIds: [...new Set(rule.orgIds)].sort(),
  }));
  rules.sort((left, right) => {
    const leftIdentity = `${left.resourceType}\u0000${left.action}`;
    const rightIdentity = `${right.resourceType}\u0000${right.action}`;
    // 相同身份的重复规则仍逐条比较，避免排序变化或去重掩盖非法输入。
    const leftValue = JSON.stringify(left);
    const rightValue = JSON.stringify(right);
    return leftIdentity < rightIdentity
      ? -1
      : leftIdentity > rightIdentity
        ? 1
        : leftValue < rightValue
          ? -1
          : leftValue > rightValue
            ? 1
            : 0;
  });
  return JSON.stringify({ defaultDataScope: state.defaultDataScope, rules });
}

export function isDataScopeAssignmentDirty(
  baseline: DataScopeAssignmentState,
  current: DataScopeAssignmentState,
): boolean {
  return comparisonSnapshot(baseline) !== comparisonSnapshot(current);
}
