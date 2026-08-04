import type { EditorState } from '@codemirror/state';

/** 有选区时执行选区，否则执行完整文档。 */
export function getExecutableScriptFromState(state: EditorState): string {
  const selection = state.selection.main;
  const selected = state.sliceDoc(selection.from, selection.to).trim();
  return selected || state.doc.toString().trim();
}
