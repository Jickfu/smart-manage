import { describe, expect, it } from 'vitest';
import {
  getAssignmentSelectionDiff,
  normalizeAssignmentIds,
  replaceAssignmentScope,
} from './assignmentSelection';

describe('assignmentSelection', () => {
  it('只替换当前范围并保留其他范围的选中项', () => {
    expect(
      replaceAssignmentScope(['app-a-1', 'app-b-1'], ['app-a-1', 'app-a-2'], ['app-a-2']),
    ).toEqual(['app-b-1', 'app-a-2']);
  });

  it('计算新增和移除关系', () => {
    expect(getAssignmentSelectionDiff(['1', '2'], ['2', '3'])).toEqual({
      addedIds: ['3'],
      removedIds: ['1'],
    });
  });

  it('提交前去除重复 ID', () => {
    expect(normalizeAssignmentIds(['1', '1', '2'])).toEqual(['1', '2']);
  });
});
