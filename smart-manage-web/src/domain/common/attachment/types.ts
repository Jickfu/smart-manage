export interface BusinessAttachment {
  id: string;
  originalName: string;
  fileSize?: number;
  mimeType?: string;
  fileExt?: string;
  isTemp?: boolean;
  uploadSessionId?: string;
  createTime?: string;
}

export interface AttachmentDownloadAccess {
  directUrl?: string;
}

export interface BusinessAttachmentFormValues {
  attachmentIds: string[];
  attachmentUploadSessions: Record<string, string>;
}
