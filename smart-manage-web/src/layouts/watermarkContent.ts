import type { ActiveUiConfig } from '@/api/uiConfig';
import type { UserInfoVO } from '@/types/api';

export const DEFAULT_WATERMARK_GAP = 100;
export const DEFAULT_WATERMARK_FONT_SIZE = 16;

/** antd Watermark 的画布宽度需要显式容纳长用户信息，避免使用默认 120px 时被裁切。 */
export const estimateWatermarkWidth = (content: string[], fontSize: number) => {
  const longestWidth = content.reduce((maximumWidth, line) => {
    const estimatedWidth = Array.from(line).reduce(
      (lineWidth, character) =>
        lineWidth + (/[^\u0000-\u00ff]/.test(character) ? fontSize : fontSize / 2),
      0,
    );
    return Math.max(maximumWidth, estimatedWidth);
  }, 0);
  return Math.min(720, Math.max(120, longestWidth + 24));
};

export const maskWatermarkPhone = (phone?: string) => {
  const normalizedPhone = phone?.trim();
  if (!normalizedPhone) return undefined;
  if (normalizedPhone.length < 7) {
    return `${normalizedPhone.slice(0, 1)}***${normalizedPhone.slice(-1)}`;
  }
  return `${normalizedPhone.slice(0, 3)}****${normalizedPhone.slice(-4)}`;
};

export const maskWatermarkEmail = (email?: string) => {
  const normalizedEmail = email?.trim();
  if (!normalizedEmail) return undefined;
  const separatorIndex = normalizedEmail.lastIndexOf('@');
  if (separatorIndex <= 0 || separatorIndex === normalizedEmail.length - 1) return '***';
  const localPart = normalizedEmail.slice(0, separatorIndex);
  const visiblePrefix = localPart.slice(0, Math.min(2, localPart.length));
  return `${visiblePrefix}***${normalizedEmail.slice(separatorIndex)}`;
};

/** 固定内容和每种用户值分别成行且不显示字段标题；空值不会生成无意义占位。 */
export const buildWatermarkContent = (
  config: ActiveUiConfig | undefined,
  user: UserInfoVO | null,
) => {
  if (!config?.watermarkEnabled || !user) return [];
  const content: string[] = [];
  const fixedContent = config.watermarkContent?.trim();
  if (fixedContent) {
    content.push(
      ...fixedContent
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter(Boolean),
    );
  }

  if (config.watermarkShowName && user.name?.trim()) content.push(user.name.trim());
  if (config.watermarkShowPhone) {
    const phone = maskWatermarkPhone(user.phone);
    if (phone) content.push(phone);
  }
  if (config.watermarkShowEmail) {
    const email = maskWatermarkEmail(user.email);
    if (email) content.push(email);
  }
  if (config.watermarkShowNumber && user.number?.trim()) {
    content.push(user.number.trim());
  }
  if (config.watermarkShowRootOrg && user.rootOrgName?.trim()) {
    content.push(user.rootOrgName.trim());
  }
  return content;
};
