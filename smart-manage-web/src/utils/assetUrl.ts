const API_BASE_PATH = '/smart-manage-api';

/** 将后端本地存储返回的资源路径补齐应用上下文，外部存储 URL 保持不变。 */
export function resolveAssetUrl(url?: string | null, fallback = ''): string {
  const normalizedUrl = url?.trim();
  if (!normalizedUrl) return fallback;
  if (normalizedUrl.startsWith('/upload/')) return `${API_BASE_PATH}${normalizedUrl}`;
  if (normalizedUrl.startsWith('upload/')) return `${API_BASE_PATH}/${normalizedUrl}`;
  if (normalizedUrl.startsWith('/sys/base/ui-config/image/')) {
    return `${API_BASE_PATH}${normalizedUrl}`;
  }
  return normalizedUrl;
}
