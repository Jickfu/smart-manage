import type { EditorState } from '@codemirror/state';

/** 只执行明确选中的 SQL；空选区和纯空白选区均不得回退执行全文。 */
export function getExecutableSqlFromState(state: EditorState): string {
  const selection = state.selection.main;
  const selectedSql = state.sliceDoc(selection.from, selection.to).trim();
  return selectedSql;
}
