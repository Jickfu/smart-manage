import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react';
import { javascript } from '@codemirror/lang-javascript';
import { EditorState } from '@codemirror/state';
import { EditorView, keymap } from '@codemirror/view';
import { basicSetup } from 'codemirror';
import { getExecutableScriptFromState } from './scriptEditorSelection';

interface ScriptEditorProps {
  value?: string;
  onChange?: (value: string) => void;
  onExecute?: (script: string) => void;
  disabled?: boolean;
  className?: string;
}

export interface ScriptEditorRef {
  getExecutableScript: () => string;
}

const ScriptEditor = forwardRef<ScriptEditorRef, ScriptEditorProps>(function ScriptEditor(
  { value = '', onChange, onExecute, disabled = false, className },
  ref,
) {
  const hostRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<EditorView | null>(null);
  const initialValueRef = useRef(value);
  const changeRef = useRef(onChange);
  const executeRef = useRef(onExecute);

  useImperativeHandle(ref, () => ({
    getExecutableScript: () =>
      editorRef.current ? getExecutableScriptFromState(editorRef.current.state) : '',
  }));

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
        javascript(),
        EditorView.lineWrapping,
        EditorView.editable.of(!disabled),
        EditorState.readOnly.of(disabled),
        EditorView.theme({ '&': { height: '100%' }, '.cm-scroller': { overflow: 'auto' } }),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) changeRef.current?.(update.state.doc.toString());
        }),
        keymap.of([
          {
            key: 'Ctrl-e',
            run: (view) => {
              if (!executeRef.current) return false;
              executeRef.current(getExecutableScriptFromState(view.state));
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
  }, [disabled]);

  return (
    <div ref={hostRef} className={['sm-script-editor', className].filter(Boolean).join(' ')} />
  );
});

export default ScriptEditor;
