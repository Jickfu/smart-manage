/// <reference types="node" />

import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';

describe('login CSP', () => {
  it('only authorizes the current inline login script by hash', () => {
    const html = readFileSync('public/login.html', 'utf8');
    const inlineScript = html.match(/<script>([\s\S]*?)<\/script>/)?.[1];

    expect(inlineScript).toBeDefined();
    // HTML 解析会将 CRLF 规范化为 LF，哈希计算必须与浏览器和 Linux 构建环境保持一致。
    const normalizedInlineScript = inlineScript!.replace(/\r\n?/g, '\n');
    const scriptHash = createHash('sha256').update(normalizedInlineScript).digest('base64');
    expect(html).toContain(`script-src 'self' 'sha256-${scriptHash}'`);
    expect(html).not.toContain("script-src 'self' 'unsafe-inline'");
  });
});

describe('login form visibility', () => {
  it('keeps the password change form hidden until it is requested', () => {
    const html = readFileSync('public/login.html', 'utf8');
    const css = readFileSync('public/css/login.css', 'utf8');

    expect(html).toMatch(/<form id="passwordChangeForm"[^>]*\shidden>/);
    expect(css).toMatch(/\.sm-form\[hidden\]\s*\{\s*display:\s*none;/);
  });

  it('opens slider verification as a hidden modal instead of rendering the retired text captcha', () => {
    const html = readFileSync('public/login.html', 'utf8');
    const css = readFileSync('public/css/login.css', 'utf8');

    expect(html).toMatch(/<div\s+id="captchaModal"[^>]*\shidden\s*>/);
    expect(html).toContain('/sys/base/captcha/challenge');
    expect(html).toContain('/sys/base/captcha/verify');
    expect(html).not.toContain('id="captchaImg"');
    expect(css).toMatch(/\.sm-captcha-modal\[hidden\]\s*\{\s*display:\s*none;/);
  });

  it('tracks drag distance from the actual pointer-down position', () => {
    const html = readFileSync('public/login.html', 'utf8');
    const css = readFileSync('public/css/login.css', 'utf8');

    expect(html).toContain('x: position.x - captchaDragStartX');
    expect(html).toContain('captchaDragStartLeft + position.x - captchaDragStartX');
    expect(css).toMatch(/\.sm-captcha-refresh\s*\{[^}]*font-size:\s*14px;/);
  });

  it('keeps email recovery in the login panel and reserves the unsupported phone entry', () => {
    const html = readFileSync('public/login.html', 'utf8');

    expect(html).toMatch(/<form id="emailRecoveryForm"[^>]*\shidden>/);
    expect(html).toMatch(/<form id="emailResetForm"[^>]*\shidden>/);
    expect(html).toContain('通过手机找回');
    expect(html).toContain('暂不支持通过手机找回密码');
    expect(html).toContain('/sys/base/login/password/email/code');
    expect(html).toContain('/sys/base/login/password/email/reset');
    expect(html).not.toContain('虚拟管理员');
  });
});
