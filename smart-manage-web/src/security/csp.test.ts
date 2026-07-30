/// <reference types="node" />

import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { describe, expect, it } from 'vitest';

describe('login CSP', () => {
  it('only authorizes the current inline login script by hash', () => {
    const html = readFileSync('public/login.html', 'utf8');
    const inlineScript = html.match(/<script>([\s\S]*?)<\/script>/)?.[1];

    expect(inlineScript).toBeDefined();
    const scriptHash = createHash('sha256').update(inlineScript!).digest('base64');
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
});
