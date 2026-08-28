import { describe, expect, it } from 'vitest';
import type { MenuVO } from '@/types/api';
import { findMenuEntry, resolveMenuAction } from './menuNavigation';

function menu(overrides: Partial<MenuVO>): MenuVO {
  return {
    id: '1',
    name: '测试菜单',
    icon: '',
    level: 1,
    routes: [],
    ...overrides,
  };
}

describe('resolveMenuAction', () => {
  it('resolves an external link opened in a new browser tab', () => {
    expect(
      resolveMenuAction(
        menu({
          targetType: 'EXTERNAL_LINK',
          externalUrl: 'https://x.com/home',
          externalOpenMode: 'NEW_TAB',
        }),
      ),
    ).toEqual({ type: 'EXTERNAL_NEW_TAB', externalUrl: 'https://x.com/home' });
  });

  it('resolves an external link opened in a workbench iframe', () => {
    expect(
      resolveMenuAction(
        menu({
          targetType: 'EXTERNAL_LINK',
          externalUrl: 'http://internal.example.test',
          externalOpenMode: 'IFRAME',
        }),
      ),
    ).toEqual({
      type: 'EXTERNAL_IFRAME',
      menuId: '1',
      title: '测试菜单',
      externalUrl: 'http://internal.example.test',
    });
  });

  it('rejects an incomplete external link configuration', () => {
    expect(() =>
      resolveMenuAction(menu({ targetType: 'EXTERNAL_LINK', externalOpenMode: 'IFRAME' })),
    ).toThrow('缺少链接地址');
  });

  it('rejects executable external link protocols from untrusted menu data', () => {
    expect(() =>
      resolveMenuAction(
        menu({
          targetType: 'EXTERNAL_LINK',
          externalUrl: 'javascript:alert(1)',
          externalOpenMode: 'NEW_TAB',
        }),
      ),
    ).toThrow('链接地址无效');
  });
});

describe('findMenuEntry', () => {
  it('finds a nested page by its stable menu number', () => {
    const page = menu({ id: '2', number: 'user_list', level: 1 });
    const group = menu({ number: 'base_group', level: 0, routes: [page] });

    expect(findMenuEntry([group], 'user_list')).toBe(page);
  });

  it('does not resolve a group as an openable entry', () => {
    const group = menu({ number: 'base_group', level: 0 });

    expect(findMenuEntry([group], 'base_group')).toBeNull();
  });
});
