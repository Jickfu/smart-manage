import { useEffect, useRef } from 'react';
import '../workflow.css';

export default function DesignerFrame({
  definitionId,
  instanceId,
  readOnly = false,
  onDirty,
  onSaved,
}: {
  definitionId?: string;
  instanceId?: string;
  readOnly?: boolean;
  onDirty?: (dirty: boolean) => void;
  onSaved?: () => void;
}) {
  const frame = useRef<HTMLIFrameElement>(null);
  const base = import.meta.env.VITE_API_BASE_PATH.replace(/\/$/, '');
  const parameters = new URLSearchParams({ readOnly: String(readOnly) });
  if (definitionId) parameters.set('definitionId', definitionId);
  if (instanceId) parameters.set('instanceId', instanceId);
  useEffect(() => {
    const handle = (event: MessageEvent) => {
      if (event.origin !== location.origin || event.source !== frame.current?.contentWindow) return;
      const message = event.data;
      if (
        !message ||
        message.channel !== 'smart-manage-workflow' ||
        message.definitionId !== (definitionId ?? null)
      )
        return;
      if (message.type === 'dirty' && typeof message.detail === 'boolean')
        onDirty?.(message.detail);
      if (message.type === 'saved') {
        onDirty?.(false);
        onSaved?.();
      }
    };
    window.addEventListener('message', handle);
    return () => window.removeEventListener('message', handle);
  }, [definitionId, onDirty, onSaved]);
  return (
    <iframe
      ref={frame}
      title={instanceId ? '本轮流程图' : '流程设计器'}
      className="sm-workflow-designer"
      src={`${base}/workflow/designer/index.html?${parameters}`}
    />
  );
}
