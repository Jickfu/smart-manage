import { readFileSync } from 'node:fs';
import { runInNewContext } from 'node:vm';
import { describe, expect, it } from 'vitest';
import { getPasswordPolicyError, passwordPolicyRule } from './passwordPolicy';

describe('固定密码策略', () => {
  it('按字符而非 UTF-16 长度检查边界，允许长口令和空格', async () => {
    expect(getPasswordPolicyError('界'.repeat(14))).toBe('密码须为15～64个字符');
    expect(getPasswordPolicyError('界'.repeat(15))).toBeUndefined();
    expect(getPasswordPolicyError('😀'.repeat(64))).toBeUndefined();
    expect(getPasswordPolicyError('😀'.repeat(65))).toBe('密码须为15～64个字符');
    expect(getPasswordPolicyError('  river garden  ')).toBeUndefined();
    await expect(
      passwordPolicyRule.validator({}, 'quiet river under moonlight'),
    ).resolves.toBeUndefined();
    await expect(passwordPolicyRule.validator({}, 'short')).rejects.toThrow('密码须为15～64个字符');
  });

  it('登录页和 React 表单对同一输入给出一致结果', () => {
    const html = readFileSync('login.html', 'utf8');
    const start = html.indexOf('      function getPasswordPolicyError(password)');
    const end = html.indexOf('      function resetPasswordByEmail()', start);
    const loginValidate = runInNewContext(`${html.slice(start, end)}; getPasswordPolicyError;`) as (
      password: string,
    ) => string | undefined;
    for (const password of [
      '',
      ' '.repeat(15),
      '\u00a0'.repeat(15),
      '\ufeff'.repeat(15),
      'quiet river under moonlight',
      '界'.repeat(14),
      '界'.repeat(15),
      '😀'.repeat(64),
      '😀'.repeat(65),
      '  river garden  ',
      'test\nlongpassword',
      '\ud800'.repeat(15),
    ]) {
      expect(loginValidate(password)).toBe(getPasswordPolicyError(password));
    }
  });
});
