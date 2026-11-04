import { describe, expect, it } from 'vitest';
import { EditorSelection, EditorState } from '@codemirror/state';
import { getExecutableSqlFromState } from './sqlEditorSelection';

describe('SQL 编辑器执行内容', () => {
  it('有选区时只返回选中的 SQL', () => {
    const document = 'select 1;\nselect 2;';
    const state = EditorState.create({
      doc: document,
      selection: EditorSelection.single(10, document.length),
    });
    expect(getExecutableSqlFromState(state)).toBe('select 2;');
  });

  it('没有选区时拒绝执行全文', () => {
    const state = EditorState.create({ doc: '  select 1;  ' });
    expect(getExecutableSqlFromState(state)).toBe('');
  });
});

it('纯空白选区不能回退执行其他 SQL', () => {
  const state = EditorState.create({
    doc: '  \nselect 1;',
    selection: EditorSelection.single(0, 3),
  });
  expect(getExecutableSqlFromState(state)).toBe('');
});
