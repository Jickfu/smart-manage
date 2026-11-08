// @vitest-environment jsdom
import { afterEach, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

afterEach(() => {
  document.head.innerHTML = '';
  document.body.innerHTML = '';
});

it('普通弹框保留声明宽度，可缩放弹框仍受最大宽度约束', () => {
  const stylesheet = document.createElement('style');
  stylesheet.textContent = readFileSync(
    resolve(process.cwd(), 'src/domain/common/component/AppModal.css'),
    'utf8',
  );
  document.head.append(stylesheet);
  document.body.innerHTML = `
    <div class="sm-app-modal-scroll">
      <div id="ordinary" class="sm-app-modal"></div>
      <div id="resizable" class="sm-app-modal sm-app-modal--resizable"></div>
    </div>`;

  expect(getComputedStyle(document.getElementById('ordinary')!).maxWidth).toBe('none');
  expect(getComputedStyle(document.getElementById('resizable')!).maxWidth).toBe(
    'max(1160px, calc(100vw - 32px))',
  );
});
