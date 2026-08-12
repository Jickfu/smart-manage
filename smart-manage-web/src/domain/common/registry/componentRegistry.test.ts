import { afterEach, describe, expect, it } from 'vitest';
import {
  componentRegistry,
  definePageRegistrations,
  getRegisteredTabTitle,
} from './componentRegistry';

const TEST_LIST_KEY = 'test/title/list';
const TEST_EDIT_KEY = 'test/title/edit';

describe('componentRegistry 页签标题规则', () => {
  afterEach(() => {
    delete componentRegistry[TEST_LIST_KEY];
    delete componentRegistry[TEST_EDIT_KEY];
  });

  it('LIST 追加列表后缀，EDIT 使用基础名称', () => {
    componentRegistry[TEST_LIST_KEY] = {
      featureKey: 'test/title',
      title: '用户',
      pageType: 'LIST',
      component: () => null,
    };
    componentRegistry[TEST_EDIT_KEY] = {
      featureKey: 'test/title',
      title: '用户',
      pageType: 'EDIT',
      component: () => null,
    };

    expect(getRegisteredTabTitle(TEST_LIST_KEY, 'LIST')).toBe('用户列表');
    expect(getRegisteredTabTitle(TEST_EDIT_KEY, 'EDIT')).toBe('用户');
  });

  it('拒绝空白基础标题', () => {
    expect(() =>
      definePageRegistrations([
        {
          featureKey: 'test/title',
          componentKey: TEST_EDIT_KEY,
          title: '   ',
          pageType: 'EDIT',
          component: () => null,
        },
      ]),
    ).toThrow('title 不能为空');
  });
});
