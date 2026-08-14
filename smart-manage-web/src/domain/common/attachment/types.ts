export interface BusinessAttachment {
  id: string;
  businessAttachmentId: string;
  originalName: string;
  fileSize?: number;
  mimeType?: string;
  fileExt?: string;
  isTemp?: boolean;
  uploadSessionId?: string;
  createTime?: string;
  uploaderId?: string;
  uploaderName?: string;
  remark?: string;
}

export interface AttachmentDownloadAccess {
  directUrl?: string;
}

export interface BusinessAttachmentFormValues {
  attachmentIds: string[];
  attachmentUploadSessions: Record<string, string>;
}
