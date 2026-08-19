export interface AssignmentSelectionDiff {
  addedIds: string[];
  removedIds: string[];
}

/** 关系分配统一使用去重后的字符串 ID，避免筛选和范围切换造成重复关系。 */
export const normalizeAssignmentIds = (ids: readonly string[]): string[] => [...new Set(ids)];

export const getAssignmentSelectionDiff = (
  initialIds: readonly string[],
  selectedIds: readonly string[],
): AssignmentSelectionDiff => {
  const initialSet = new Set(initialIds);
  const selectedSet = new Set(selectedIds);
  return {
    addedIds: [...selectedSet].filter((id) => !initialSet.has(id)),
    removedIds: [...initialSet].filter((id) => !selectedSet.has(id)),
  };
};

/** 只替换当前可见范围内的选择，完整保留其他应用、功能或筛选结果中的关系。 */
export const replaceAssignmentScope = (
  selectedIds: readonly string[],
  scopeIds: readonly string[],
  selectedScopeIds: readonly string[],
): string[] => {
  const scopeSet = new Set(scopeIds);
  const nextIds = selectedIds.filter((id) => !scopeSet.has(id));
  const selectedScopeSet = new Set(selectedScopeIds);
  for (const id of scopeIds) {
    if (selectedScopeSet.has(id)) nextIds.push(id);
  }
  return normalizeAssignmentIds(nextIds);
};
