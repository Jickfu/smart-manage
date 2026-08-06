export interface UiConfigDetail {
  id?: string;
  version?: number;
  pageTitle?: string;
  systemName?: string;
  loginBanner?: string;
  loginLogo?: string;
  headerLogo?: string;
  loginBannerAttachmentId?: string;
  loginLogoAttachmentId?: string;
  headerLogoAttachmentId?: string;
  attachmentUploadSessions?: Record<string, string>;
}

export interface AttachmentVO {
  id: string;
  originalName: string;
  fileSize?: number;
  mimeType?: string;
  isTemp: boolean;
  uploadSessionId?: string;
  url: string;
}
