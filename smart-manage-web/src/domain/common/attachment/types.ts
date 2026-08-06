export interface BusinessAttachment {
  id: string;
  originalName: string;
  fileSize?: number;
  mimeType?: string;
  fileExt?: string;
  isTemp?: boolean;
  uploadSessionId?: string;
  url?: string;
  createTime?: string;
}

export interface BusinessAttachmentFormValues {
  attachmentIds: string[];
  attachmentUploadSessions: Record<string, string>;
}
