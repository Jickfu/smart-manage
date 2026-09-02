import { describe, expect, it } from 'vitest';
import { deriveListSelection } from './useListSelection';

describe('列表选择派生', () => {
  const records = [{ id: 'one' }, { id: 'two' }];

  it('空选择不产生记录或单选', () => {
    expect(deriveListSelection(records, [])).toEqual({
      selectedIds: [],
      selectedRecords: [],
      singleSelectedRecord: undefined,
    });
  });

  it('单选保留当前记录引用', () => {
    expect(deriveListSelection(records, ['two']).singleSelectedRecord).toBe(records[1]);
  });

  it('当前页只命中一条也不能把跨页多选误判为单选', () => {
    expect(deriveListSelection(records, ['one', 'missing'])).toEqual({
      selectedIds: ['one', 'missing'],
      selectedRecords: [records[0]],
      singleSelectedRecord: undefined,
    });
  });

  it('键转换只用于命令 ID，记录匹配保留原有严格键语义', () => {
    expect(deriveListSelection([{ id: '1' }], [1])).toEqual({
      selectedIds: ['1'],
      selectedRecords: [],
      singleSelectedRecord: undefined,
    });
  });

  it('数据刷新仅重新派生，不修改选中键或输入记录', () => {
    const selectedKeys = Object.freeze(['one']);
    const refreshed = Object.freeze([{ id: 'one', name: 'new' }]);
    expect(deriveListSelection([], selectedKeys).selectedIds).toEqual(['one']);
    expect(deriveListSelection(refreshed, selectedKeys).singleSelectedRecord).toBe(refreshed[0]);
    expect(selectedKeys).toEqual(['one']);
  });
});
