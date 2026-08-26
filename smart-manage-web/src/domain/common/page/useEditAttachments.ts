import { useState } from 'react';
import type {
  BusinessAttachment,
  BusinessAttachmentFormValues,
} from '@/domain/common/attachment/types';

interface AttachmentResource {
  resourceType: string;
  initialAttachments?: BusinessAttachment[];
}

export function buildAttachmentFormValues(
  values: Record<string, unknown>,
  attachments: BusinessAttachment[],
): Record<string, unknown> & BusinessAttachmentFormValues {
  return {
    ...values,
    attachmentIds: attachments.map((attachment) => attachment.id),
    attachmentUploadSessions: Object.fromEntries(
      attachments
        .filter((attachment) => attachment.isTemp && attachment.uploadSessionId)
        .map((attachment) => [attachment.id, attachment.uploadSessionId!]),
    ),
  };
}

export function useEditAttachments(
  resource: AttachmentResource | undefined,
  markDirty: () => void,
) {
  const [state, setState] = useState<{
    source: BusinessAttachment[] | undefined;
    values: BusinessAttachment[];
  }>({ source: undefined, values: [] });
  const attachments =
    state.source === resource?.initialAttachments
      ? state.values
      : (resource?.initialAttachments ?? []);

  return {
    attachments,
    withValues: (values: Record<string, unknown>) =>
      resource ? buildAttachmentFormValues(values, attachments) : values,
    update: (values: BusinessAttachment[], changeType: 'upload' | 'delete' | 'metadata') => {
      if (changeType === 'upload') markDirty();
      setState({ source: resource?.initialAttachments, values });
    },
  };
}
