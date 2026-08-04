import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react';
import { basicSetup } from 'codemirror';
import { PostgreSQL, sql } from '@codemirror/lang-sql';
import { EditorView, keymap } from '@codemirror/view';
import { EditorState } from '@codemirror/state';
import { getExecutableSqlFromState } from './sqlEditorSelection';

interface SqlEditorProps {
  value: string;
  onChange: (value: string) => void;
  onExecute: (sql: string) => void;
}

export interface SqlEditorRef {
  getExecutableSql: () => string;
}

/** 有选区时返回选区 SQL，否则返回完整文档。 */
function getExecutableSql(view: EditorView | null): string {
  return view ? getExecutableSqlFromState(view.state) : '';
}

const SqlEditor = forwardRef<SqlEditorRef, SqlEditorProps>(function SqlEditor(
  { value, onChange, onExecute },
  ref,
) {
  const hostRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<EditorView | null>(null);
  const initialValueRef = useRef(value);
  const changeRef = useRef(onChange);
  const executeRef = useRef(onExecute);

  useImperativeHandle(ref, () => ({ getExecutableSql: () => getExecutableSql(editorRef.current) }));

  useEffect(() => {
    changeRef.current = onChange;
    executeRef.current = onExecute;
  }, [onChange, onExecute]);

  useEffect(() => {
    const editor = editorRef.current;
    if (!editor || editor.state.doc.toString() === value) return;
    editor.dispatch({ changes: { from: 0, to: editor.state.doc.length, insert: value } });
  }, [value]);

  useEffect(() => {
    if (!hostRef.current) return;
    const state = EditorState.create({
      doc: initialValueRef.current,
      extensions: [
        basicSetup,
        sql({ dialect: PostgreSQL, upperCaseKeywords: true }),
        EditorView.lineWrapping,
        EditorView.theme({ '&': { height: '100%' }, '.cm-scroller': { overflow: 'auto' } }),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) changeRef.current(update.state.doc.toString());
        }),
        keymap.of([
          {
            key: 'Ctrl-e',
            run: (view) => {
              executeRef.current(getExecutableSql(view));
              return true;
            },
          },
        ]),
      ],
    });
    const editor = new EditorView({ state, parent: hostRef.current });
    editorRef.current = editor;
    return () => {
      editorRef.current = null;
      editor.destroy();
    };
  }, []);

  return <div ref={hostRef} className="sm-sql-editor" />;
});

export default SqlEditor;
