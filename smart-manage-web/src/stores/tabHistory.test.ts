import { describe, expect, it } from 'vitest';
import { pushTabHistory, resolveNextActiveTabKey } from './tabHistory';

describe('tabHistory', () => {
  it('将最近激活页签去重后放到历史末尾', () => {
    expect(pushTabHistory(['home', 'apps', 'order'], 'apps')).toEqual(['home', 'order', 'apps']);
  });

  it('选择最近仍存在且未被排除的页签', () => {
    const availableKeys = new Set(['home', 'apps', 'order']);
    const excludedKeys = new Set(['order']);

    expect(
      resolveNextActiveTabKey(
        ['home', 'apps', 'removed', 'order'],
        availableKeys,
        'home',
        excludedKeys,
      ),
    ).toBe('apps');
  });

  it('没有可用历史记录时返回指定兜底页签', () => {
    expect(resolveNextActiveTabKey(['removed'], new Set(), '__home__')).toBe('__home__');
  });
});
