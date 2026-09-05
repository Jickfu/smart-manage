import { useEffect, useRef } from 'react';
import { javascript } from '@codemirror/lang-javascript';
import { EditorState } from '@codemirror/state';
import { EditorView } from '@codemirror/view';
import { basicSetup } from 'codemirror';
import './JsonCodeEditor.css';

export interface JsonCodeEditorProps {
  value?: string;
  onChange?: (value: string) => void;
  readOnly?: boolean;
  ariaLabel?: string;
}

/** 通用 JSON 编辑器；兼容 Ant Design Form 的 value/onChange 受控组件协议。 */
export default function JsonCodeEditor({
  value = '',
  onChange,
  readOnly = false,
  ariaLabel = 'JSON 编辑器',
}: JsonCodeEditorProps) {
  const hostRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<EditorView | null>(null);
  const initialValueRef = useRef(value);
  const onChangeRef = useRef(onChange);

  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

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
        EditorState.readOnly.of(readOnly),
        EditorView.editable.of(!readOnly),
        EditorView.lineWrapping,
        EditorView.contentAttributes.of({ 'aria-label': ariaLabel }),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) onChangeRef.current?.(update.state.doc.toString());
        }),
      ],
    });
    const editor = new EditorView({ state, parent: hostRef.current });
    editorRef.current = editor;
    return () => {
      editorRef.current = null;
      editor.destroy();
    };
  }, [ariaLabel, readOnly]);

  return <div ref={hostRef} className="sm-json-code-editor" />;
}
