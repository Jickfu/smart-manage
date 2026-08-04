import { describe, expect, it } from 'vitest';
import { EditorSelection, EditorState } from '@codemirror/state';
import { getExecutableScriptFromState } from './scriptEditorSelection';

describe('脚本编辑器执行内容', () => {
  it('有选区时只返回选中脚本', () => {
    const document = 'console.log(1);\nconsole.log(2);';
    const state = EditorState.create({
      doc: document,
      selection: EditorSelection.single(16, document.length),
    });
    expect(getExecutableScriptFromState(state)).toBe('console.log(2);');
  });

  it('无选区时返回完整脚本', () => {
    const state = EditorState.create({ doc: '  return 1;  ' });
    expect(getExecutableScriptFromState(state)).toBe('return 1;');
  });
});
