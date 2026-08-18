import { beforeEach, describe, expect, it } from 'vitest';
import { useHeaderTabsStore } from './headerTabs';

describe('header tabs store', () => {
  beforeEach(() => {
    useHeaderTabsStore.setState({
      tabs: [
        { key: 'home', label: '首页', type: 'system', pinned: false, loaded: true },
        { key: 'apps', label: '应用', type: 'system', pinned: false, loaded: true },
      ],
      activeKey: 'home',
      activeHistory: ['home'],
    });
  });

  it('登录初始化固定标签但不标记为已加载', () => {
    useHeaderTabsStore.getState().initializePinnedApps([
      { number: 'base', name: '系统建模' },
      { number: 'monitor', name: '系统监控' },
    ]);

    expect(useHeaderTabsStore.getState().tabs.slice(2)).toEqual([
      {
        key: 'base',
        label: '系统建模',
        type: 'app',
        pinned: true,
        loaded: false,
      },
      {
        key: 'monitor',
        label: '系统监控',
        type: 'app',
        pinned: true,
        loaded: false,
      },
    ]);
  });

  it('点击固定应用完成加载后保留固定状态和原顺序', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([
      { number: 'base', name: '系统建模' },
      { number: 'monitor', name: '系统监控' },
    ]);
    store.addAppTab('base', '系统建模');

    const appTabs = useHeaderTabsStore.getState().tabs.slice(2);
    expect(appTabs.map((tab) => tab.key)).toEqual(['base', 'monitor']);
    expect(appTabs[0]).toMatchObject({ pinned: true, loaded: true });
  });

  it('新固定应用追加到固定区末尾且解锁后移动到普通区末尾', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([{ number: 'base', name: '系统建模' }]);
    store.addAppTab('procurement', '采购管理');
    store.addAppTab('monitor', '系统监控');

    store.setAppPinned('monitor', true);
    expect(
      useHeaderTabsStore
        .getState()
        .tabs.slice(2)
        .map((tab) => tab.key),
    ).toEqual(['base', 'monitor', 'procurement']);

    store.setAppPinned('base', false);
    expect(
      useHeaderTabsStore
        .getState()
        .tabs.slice(2)
        .map((tab) => tab.key),
    ).toEqual(['monitor', 'procurement', 'base']);
  });

  it('状态层拒绝关闭固定应用', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([{ number: 'base', name: '系统建模' }]);

    store.removeAppTab('base');

    expect(useHeaderTabsStore.getState().tabs.some((tab) => tab.key === 'base')).toBe(true);
  });
});
