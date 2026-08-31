import dayjs from 'dayjs';
import { describe, expect, it } from 'vitest';
import { formatCredentialExpiresAt } from './credentialForm';

describe('formatCredentialExpiresAt', () => {
  it('使用后端 LocalDateTime 支持的空格分隔格式', () => {
    expect(formatCredentialExpiresAt(dayjs('2026-08-31 21:22:37'))).toBe('2026-08-31 21:22:37');
  });

  it('未设置过期时间时不发送字段值', () => {
    expect(formatCredentialExpiresAt()).toBeUndefined();
  });
});
