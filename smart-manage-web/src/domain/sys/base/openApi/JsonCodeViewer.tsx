import { useEffect, useRef } from 'react';
import { javascript } from '@codemirror/lang-javascript';
import { EditorState } from '@codemirror/state';
import { EditorView } from '@codemirror/view';
import { basicSetup } from 'codemirror';

interface JsonCodeViewerProps {
  value: string;
}

/** API 文档专用的只读 JSON 查看器，避免把可编辑能力暴露到文档页。 */
export default function JsonCodeViewer({ value }: JsonCodeViewerProps) {
  const hostRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<EditorView | null>(null);
  const initialValueRef = useRef(value);

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
        EditorState.readOnly.of(true),
        EditorView.editable.of(false),
        EditorView.lineWrapping,
      ],
    });
    const editor = new EditorView({ state, parent: hostRef.current });
    editorRef.current = editor;
    return () => {
      editorRef.current = null;
      editor.destroy();
    };
  }, []);

  return <div ref={hostRef} className="sm-openapi-json-viewer" />;
}
