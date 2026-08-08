export interface UiConfigDetail {
  id?: string;
  version?: number;
  pageTitle?: string;
  systemName?: string;
  loginBanner?: string | null;
  loginLogo?: string | null;
  headerLogo?: string | null;
  loginBannerAttachmentId?: string | null;
  loginLogoAttachmentId?: string | null;
  headerLogoAttachmentId?: string | null;
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
