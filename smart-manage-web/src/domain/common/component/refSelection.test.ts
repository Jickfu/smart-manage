import { describe, expect, it } from 'vitest';
import { createRefSelection, mergeRefTableSelection } from './refSelection';

interface RecordItem extends Record<string, unknown> {
  id: string;
  name: string;
}

describe('引用选择状态', () => {
  it('从外部多选值建立选择池', () => {
    const selection = createRefSelection<RecordItem>(
      [
        { id: '1', name: '甲' },
        { id: '2', name: '乙' },
      ],
      true,
      'id',
    );
    expect([...selection.keys()]).toEqual(['1', '2']);
  });

  it('跨页变更只删除反选项并保留非当前页记录', () => {
    const previous = new Map<string, RecordItem>([
      ['1', { id: '1', name: '第一页' }],
      ['2', { id: '2', name: '第二页旧项' }],
    ]);
    const next = mergeRefTableSelection(
      previous,
      ['1', '2'],
      ['1', '3'],
      [{ id: '3', name: '新项' }],
      'id',
    );
    expect([...next.keys()]).toEqual(['1', '3']);
    expect(next.get('1')?.name).toBe('第一页');
  });
});
