// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const globalCss = readFileSync(resolve(process.cwd(), 'src/styles/global.css'), 'utf8');

afterEach(() => {
  document.head.innerHTML = '';
  document.body.innerHTML = '';
});

function mountTableLinks() {
  const style = document.createElement('style');
  style.textContent = globalCss;
  document.head.append(style);
  document.body.innerHTML = `
    <button id="toolbar" class="ant-btn ant-btn-variant-link">工具栏</button>
    <div class="ant-table-wrapper"><table><tbody class="ant-table-tbody"><tr><td>
      <button id="native" type="button" class="sm-table-link">姓名</button>
      <button id="antd" type="button" class="ant-btn ant-btn-variant-link"><span>编码</span></button>
      <button id="disabled" disabled class="ant-btn ant-btn-variant-link">禁用</button>
      <a id="disabledLink" class="ant-btn ant-btn-variant-link ant-btn-disabled">禁用链接</a>
    </td></tr></tbody></table></div>`;
  return Array.from(style.sheet!.cssRules).filter(
    (rule): rule is CSSStyleRule => rule instanceof CSSStyleRule,
  );
}

describe('表格文本入口公共样式', () => {
  it('原生按钮和 Ant Design 链接按钮均可选择文字，且不扩大到工具栏', () => {
    mountTableLinks();
    for (const identity of ['native', 'antd']) {
      expect(getComputedStyle(document.getElementById(identity)!).userSelect).toBe('text');
    }
    expect(getComputedStyle(document.getElementById('toolbar')!).userSelect).not.toBe('text');
  });

  it('两种入口共用悬停下划线规则，排除禁用入口和工具栏', () => {
    const rules = mountTableLinks();
    // jsdom 不执行真实鼠标悬停；这里只校验 CSS 选择器覆盖范围，实际拖选由浏览器验收。
    const hoverSelectors = rules
      .filter(
        (rule) =>
          rule.selectorText.includes(':hover') &&
          rule.style.getPropertyValue('text-decoration') === 'underline',
      )
      .map((rule) => rule.selectorText.replaceAll(':hover', ''));
    for (const identity of ['native', 'antd']) {
      const element = document.getElementById(identity)!;
      expect(hoverSelectors.some((selector) => element.matches(selector))).toBe(true);
      expect(getComputedStyle(element).textDecoration).not.toContain('underline');
    }
    for (const identity of ['disabled', 'disabledLink', 'toolbar']) {
      const element = document.getElementById(identity)!;
      expect(hoverSelectors.some((selector) => element.matches(selector))).toBe(false);
    }
  });
});
