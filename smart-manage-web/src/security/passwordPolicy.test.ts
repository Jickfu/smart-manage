import { readFileSync } from 'node:fs';
import { runInNewContext } from 'node:vm';
import { describe, expect, it } from 'vitest';
import {
  getPasswordPolicyError as validatePassword,
  createPasswordPolicyRule,
} from './passwordPolicy';

const policy = { minLength: 15, maxLength: 64, complexityEnabled: false };
const getPasswordPolicyError = (password: string) => validatePassword(password, policy);
const passwordPolicyRule = createPasswordPolicyRule(policy);

describe('可配置密码策略', () => {
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
    const start = html.indexOf('      function getPasswordPolicyError(password, policy)');
    const end = html.indexOf('      async function resetPasswordByEmail()', start);
    expect(start).toBeGreaterThanOrEqual(0);
    expect(end).toBeGreaterThan(start);
    const loginValidate = runInNewContext(`${html.slice(start, end)}; getPasswordPolicyError;`) as (
      password: string,
      policy: { minLength: number; maxLength: number; complexityEnabled: boolean },
    ) => string | undefined;
    for (const currentPolicy of [
      policy,
      { minLength: 4, maxLength: 64, complexityEnabled: true },
      { minLength: 64, maxLength: 64, complexityEnabled: false },
    ]) {
      for (const password of [
        'Ab7!',
        'ab7!',
        'AB7!',
        'Abc!',
        'Ab78',
        'Ab7 ',
        'Ab7中',
        'Ab7😀',
        'Ab7！',
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
        expect(loginValidate(password, currentPolicy)).toBe(
          validatePassword(password, currentPolicy),
        );
      }
    }
  });
  it('策略未加载时阻止提交', async () => {
    await expect(createPasswordPolicyRule(undefined).validator({}, 'Ab7!')).rejects.toThrow(
      '密码策略尚未加载',
    );
  });
});
