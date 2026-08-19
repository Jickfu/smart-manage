import { create } from 'zustand';
import { pushTabHistory, resolveNextActiveTabKey } from './tabHistory';

export interface HeaderTabItem {
  key: string;
  label: string;
  type: 'system' | 'app';
  pinned: boolean;
  loaded: boolean;
}

export interface PinnedHeaderApp {
  number: string;
  name: string;
}

interface HeaderTabsState {
  tabs: HeaderTabItem[];
  activeKey: string;
  /** 切换历史 — 关闭 tab 时智能回到上一个激活的 tab */
  activeHistory: string[];
  /** 纯状态切换 — 不操作 URL，由 navigationService 统一控制 URL 同步 */
  activate: (key: string) => void;
  /** 登录后只恢复固定标签，不创建应用工作台。 */
  initializePinnedApps: (apps: PinnedHeaderApp[]) => void;
  /** 纯状态添加 — 不操作 URL */
  addAppTab: (key: string, label: string) => void;
  /** 纯状态移除 — 不操作 URL */
  removeAppTab: (key: string) => void;
  setAppPinned: (key: string, pinned: boolean) => void;
}

export const useHeaderTabsStore = create<HeaderTabsState>((set, get) => ({
  tabs: [
    { key: 'home', label: '首页', type: 'system', pinned: false, loaded: true },
    { key: 'apps', label: '应用', type: 'system', pinned: false, loaded: true },
  ],
  activeKey: 'home',
  activeHistory: ['home'],

  activate: (key) =>
    set((state) => ({
      activeKey: key,
      activeHistory: pushTabHistory(state.activeHistory, key),
    })),

  initializePinnedApps: (apps) =>
    set((state) => {
      const systemTabs = state.tabs.filter((tab) => tab.type === 'system');
      const existingApps = new Map(
        state.tabs.filter((tab) => tab.type === 'app').map((tab) => [tab.key, tab]),
      );
      const pinnedTabs = apps.map((app) => {
        const existing = existingApps.get(app.number);
        existingApps.delete(app.number);
        return {
          key: app.number,
          label: app.name,
          type: 'app' as const,
          pinned: true,
          loaded: existing?.loaded ?? false,
        };
      });
      return { tabs: [...systemTabs, ...pinnedTabs, ...existingApps.values()] };
    }),

  addAppTab: (key, label) => {
    const { tabs, activeHistory } = get();
    const exists = tabs.find((tab) => tab.key === key);
    if (exists) {
      set({
        tabs: tabs.map((tab) => (tab.key === key ? { ...tab, label, loaded: true } : tab)),
        activeKey: key,
        activeHistory: pushTabHistory(activeHistory, key),
      });
      return;
    }
    set({
      tabs: [...tabs, { key, label, type: 'app', pinned: false, loaded: true }],
      activeKey: key,
      activeHistory: pushTabHistory(activeHistory, key),
    });
  },

  removeAppTab: (key) => {
    const { tabs, activeKey, activeHistory } = get();
    const target = tabs.find((tab) => tab.key === key);
    if (target?.type !== 'app' || target.pinned) return;
    const nextTabs = tabs.filter((tab) => tab.key !== key);
    const nextHistory = activeHistory.filter((item) => item !== key);
    const nextActiveKey =
      activeKey === key
        ? resolveNextActiveTabKey(
            activeHistory,
            new Set(nextTabs.map((tab) => tab.key)),
            'apps',
            new Set([key]),
          )
        : activeKey;
    set({
      tabs: nextTabs,
      activeKey: nextActiveKey,
      activeHistory: pushTabHistory(nextHistory, nextActiveKey),
    });
  },

  setAppPinned: (key, pinned) =>
    set((state) => {
      const target = state.tabs.find((tab) => tab.key === key);
      if (target?.type !== 'app' || target.pinned === pinned) return state;
      return {
        // 固定状态只影响按钮和关闭能力，不能打断用户当前的应用标签排列。
        tabs: state.tabs.map((tab) => (tab.key === key ? { ...tab, pinned } : tab)),
      };
    }),
}));
