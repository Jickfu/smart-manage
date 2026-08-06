export interface AttachmentConfigDetail {
  id: string;
  version: number;
  maxUploadBytes: number;
  allowedExtensions: string[];
  allowedMimeTypes: string[];
  tempExpireHours: number;
}

export type AttachmentConfigSaveForm = AttachmentConfigDetail;
