import type { Dayjs } from 'dayjs';

/** 与后端全局 LocalDateTime JSON 契约保持一致，禁止使用 ISO 中的 T 分隔符。 */
export const formatCredentialExpiresAt = (value?: Dayjs): string | undefined =>
  value?.format('YYYY-MM-DD HH:mm:ss');
