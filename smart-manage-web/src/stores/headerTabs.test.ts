import { beforeEach, describe, expect, it } from 'vitest';
import { useHeaderTabsStore } from './headerTabs';

describe('header tabs store', () => {
  it('消息中心固定恢复、打开、解锁关闭沿用应用页签生命周期', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([{ number: 'builtin:inbox', name: '消息中心' }]);
    expect(
      useHeaderTabsStore.getState().tabs.find((tab) => tab.key === 'builtin:inbox'),
    ).toMatchObject({ type: 'inbox', pinned: true, loaded: false });
    const receipt = { messageId: '30', receivedTime: '2026-09-01 12:00:00.123456' };
    store.openInbox('messages', receipt);
    expect(useHeaderTabsStore.getState().inboxTarget.receipt).toEqual(receipt);
    expect(useHeaderTabsStore.getState().tabs.filter((tab) => tab.type === 'inbox')).toHaveLength(
      1,
    );
    store.removeAppTab('builtin:inbox');
    expect(useHeaderTabsStore.getState().activeKey).toBe('builtin:inbox');
    store.setAppPinned('builtin:inbox', false);
    store.removeAppTab('builtin:inbox');
    expect(useHeaderTabsStore.getState().activeKey).toBe('home');
    expect(useHeaderTabsStore.getState().tabs.some((tab) => tab.type === 'inbox')).toBe(false);
  });
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
      { number: 'base', name: '系统管理' },
      { number: 'monitor', name: '运维中心' },
    ]);

    expect(useHeaderTabsStore.getState().tabs.slice(2)).toEqual([
      {
        key: 'base',
        label: '系统管理',
        type: 'app',
        pinned: true,
        loaded: false,
      },
      {
        key: 'monitor',
        label: '运维中心',
        type: 'app',
        pinned: true,
        loaded: false,
      },
    ]);
  });

  it('点击固定应用完成加载后保留固定状态和原顺序', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([
      { number: 'base', name: '系统管理' },
      { number: 'monitor', name: '运维中心' },
    ]);
    store.addAppTab('base', '系统管理');

    const appTabs = useHeaderTabsStore.getState().tabs.slice(2);
    expect(appTabs.map((tab) => tab.key)).toEqual(['base', 'monitor']);
    expect(appTabs[0]).toMatchObject({ pinned: true, loaded: true });
  });

  it('加锁和解锁只改变固定状态且保持标签位置不变', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([{ number: 'base', name: '系统管理' }]);
    store.addAppTab('procurement', '采购管理');
    store.addAppTab('monitor', '运维中心');

    store.setAppPinned('monitor', true);
    expect(
      useHeaderTabsStore
        .getState()
        .tabs.slice(2)
        .map((tab) => tab.key),
    ).toEqual(['base', 'procurement', 'monitor']);
    expect(useHeaderTabsStore.getState().tabs.find((tab) => tab.key === 'monitor')?.pinned).toBe(
      true,
    );

    store.setAppPinned('base', false);
    expect(
      useHeaderTabsStore
        .getState()
        .tabs.slice(2)
        .map((tab) => tab.key),
    ).toEqual(['base', 'procurement', 'monitor']);
    expect(useHeaderTabsStore.getState().tabs.find((tab) => tab.key === 'base')?.pinned).toBe(
      false,
    );
  });

  it('状态层拒绝关闭固定应用', () => {
    const store = useHeaderTabsStore.getState();
    store.initializePinnedApps([{ number: 'base', name: '系统管理' }]);

    store.removeAppTab('base');

    expect(useHeaderTabsStore.getState().tabs.some((tab) => tab.key === 'base')).toBe(true);
  });
});
