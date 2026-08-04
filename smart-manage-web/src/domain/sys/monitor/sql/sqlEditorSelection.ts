import type { EditorState } from '@codemirror/state';

/** 有选区时返回选区 SQL，否则返回完整文档。 */
export function getExecutableSqlFromState(state: EditorState): string {
  const selection = state.selection.main;
  const selectedSql = state.sliceDoc(selection.from, selection.to).trim();
  return selectedSql || state.doc.toString().trim();
}
