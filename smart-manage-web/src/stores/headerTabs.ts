import { create } from 'zustand';
import { pushTabHistory, resolveNextActiveTabKey } from './tabHistory';

export interface HeaderTabItem {
  key: string;
  label: string;
  closable: boolean;
}

interface HeaderTabsState {
  tabs: HeaderTabItem[];
  activeKey: string;
  /** 切换历史 — 关闭 tab 时智能回到上一个激活的 tab */
  activeHistory: string[];
  /** 纯状态切换 — 不操作 URL，由 navigationService 统一控制 URL 同步 */
  activate: (key: string) => void;
  /** 纯状态添加 — 不操作 URL */
  addAppTab: (key: string, label: string) => void;
  /** 纯状态移除 — 不操作 URL */
  removeAppTab: (key: string) => void;
}

export const useHeaderTabsStore = create<HeaderTabsState>((set, get) => ({
  tabs: [
    { key: 'home', label: '首页', closable: false },
    { key: 'apps', label: '应用', closable: false },
  ],
  activeKey: 'home',
  activeHistory: ['home'],

  activate: (key) =>
    set((state) => ({
      activeKey: key,
      activeHistory: pushTabHistory(state.activeHistory, key),
    })),

  addAppTab: (key, label) => {
    const { tabs, activeHistory } = get();
    const exists = tabs.find((tab) => tab.key === key);
    if (exists) {
      set({ activeKey: key, activeHistory: pushTabHistory(activeHistory, key) });
      return;
    }
    set({
      tabs: [...tabs, { key, label, closable: true }],
      activeKey: key,
      activeHistory: pushTabHistory(activeHistory, key),
    });
  },

  removeAppTab: (key) => {
    const { tabs, activeKey, activeHistory } = get();
    const target = tabs.find((tab) => tab.key === key);
    if (!target?.closable) return;
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
}));
