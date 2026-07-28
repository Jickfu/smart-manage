import { create } from 'zustand';
import {
  createAddNewTabKey,
  createBillTabKey,
  createListTabKey,
} from '@/domain/common/page/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { AppVO } from '@/domain/sys/app/types';
import { pushTabHistory, resolveNextActiveTabKey } from './tabHistory';

/** 内容页签最大数量（不含首页） */
const MAX_CONTENT_TABS = 20;

/** addContentTab 返回结果 */
export type AddTabResult = 'opened' | 'activated' | 'limit_reached';

export interface ContentTabItem {
  key: string;
  label: string;
  closable: boolean;
  componentKey?: string;
  pageType?: 'LIST' | 'EDIT' | 'CUSTOM';
  operationType?: OperationType;
  billId?: string;
  /** 新增页临时标记 — 保存后替换为真实单据 tab */
  temporary?: boolean;
}

interface WorkspaceState {
  appInfo: AppVO;
  contentTabs: ContentTabItem[];
  activeContentTabKey: string;
  activeContentTabHistory: string[];
}

/**
 * 页面关闭前检查回调 — 页面组件通过 store 注册，关闭 tab 时调用。
 * 返回 false 则阻止关闭。
 */
type BeforeCloseFn = () => Promise<boolean>;

/** 构造 beforeClose 回调键 — appNumber:tabKey，避免不同应用间冲突 */
function callbackKey(appNumber: string, tabKey: string): string {
  return `${appNumber}:${tabKey}`;
}

interface WorkbenchState {
  workspaces: Record<string, WorkspaceState>;
  /** appNumber:tabKey → beforeClose 回调映射 */
  beforeCloseCallbacks: Record<string, BeforeCloseFn>;
  initWorkspace: (appNumber: string, appInfo: AppVO) => void;
  destroyWorkspace: (appNumber: string) => void;
  /** 异步关闭 Workspace — 先顺序检查所有内容页 beforeClose，全部通过后一次性销毁 */
  closeWorkspace: (appNumber: string) => Promise<boolean>;
  /** 异步批量关闭内容页签 — 顺序检查 beforeClose，全部通过后原子移除 */
  closeContentTabs: (appNumber: string, tabKeys: string[]) => Promise<boolean>;
  /** 检查所有 Workspace 是否有未保存数据（供退出登录等全局操作使用） */
  checkAllDirty: () => Promise<boolean>;
  /** 页面组件注册关闭前检查回调 */
  registerBeforeClose: (appNumber: string, tabKey: string, fn: BeforeCloseFn) => void;
  /** 页面组件注销关闭前检查回调 */
  unregisterBeforeClose: (appNumber: string, tabKey: string) => void;
  /** 添加/激活 content tab — 返回操作结果供调用层反馈 */
  addContentTab: (appNumber: string, tab: ContentTabItem) => AddTabResult;
  openListTab: (appNumber: string, componentKey: string, label: string) => AddTabResult;
  openAddNewTab: (appNumber: string, componentKey: string, label: string) => AddTabResult;
  openBillTab: (
    appNumber: string,
    componentKey: string,
    label: string,
    billId: string,
    operationType: OperationType,
  ) => AddTabResult;
  replaceContentTab: (appNumber: string, oldTabKey: string, nextTab: ContentTabItem) => void;
  removeContentTab: (appNumber: string, tabKey: string) => Promise<void>;
  activateContentTab: (appNumber: string, tabKey: string) => void;
}

function defaultWorkspace(appInfo: AppVO): WorkspaceState {
  return {
    appInfo,
    contentTabs: [{ key: '__home__', label: '应用首页', closable: false }],
    activeContentTabKey: '__home__',
    activeContentTabHistory: ['__home__'],
  };
}

export const useWorkbenchStore = create<WorkbenchState>((set, get) => ({
  workspaces: {},
  beforeCloseCallbacks: {},

  initWorkspace: (appNumber, appInfo) => {
    const { workspaces } = get();
    if (workspaces[appNumber]) return;
    set({ workspaces: { ...workspaces, [appNumber]: defaultWorkspace(appInfo) } });
  },

  destroyWorkspace: (appNumber) => {
    const { workspaces, beforeCloseCallbacks } = get();
    if (!workspaces[appNumber]) return;
    const ws = workspaces[appNumber];
    const nextCallbacks = { ...beforeCloseCallbacks };
    for (const tab of ws.contentTabs) {
      delete nextCallbacks[callbackKey(appNumber, tab.key)];
    }
    const next = { ...workspaces };
    delete next[appNumber];
    set({ workspaces: next, beforeCloseCallbacks: nextCallbacks });
  },

  closeWorkspace: async (appNumber) => {
    const ws = get().workspaces[appNumber];
    if (!ws) return true;

    // 迭代检查：异步检查期间可能新增页签，需要循环检查直到没有未检查的页签
    const checkedKeys = new Set<string>();
    const MAX_ROUNDS = 5;

    for (let round = 0; round < MAX_ROUNDS; round++) {
      const currentWs = get().workspaces[appNumber];
      if (!currentWs) return true;

      // 找出本轮的未检查页签（排除首页）
      const unchecked = currentWs.contentTabs.filter(
        (t) => t.key !== '__home__' && !checkedKeys.has(t.key),
      );

      if (unchecked.length === 0) break; // 已全部检查完毕

      for (const tab of unchecked) {
        const key = callbackKey(appNumber, tab.key);
        // 每个页签检查前重新获取最新回调映射，避免同一轮内前一个异步检查期间注册的回调被遗漏
        const beforeClose = get().beforeCloseCallbacks[key];
        if (beforeClose) {
          try {
            const canClose = await beforeClose();
            if (!canClose) return false;
          } catch {
            return false;
          }
        }
        checkedKeys.add(tab.key);
      }
    }

    // 检查轮次超限：页签持续新增，放弃关闭
    const finalWs = get().workspaces[appNumber];
    if (finalWs) {
      const stillUnchecked = finalWs.contentTabs.filter(
        (t) => t.key !== '__home__' && !checkedKeys.has(t.key),
      );
      if (stillUnchecked.length > 0) {
        console.warn(
          `[workbench] closeWorkspace "${appNumber}" 检查轮次超限，仍有 ${stillUnchecked.length} 个未检查页签`,
        );
        return false;
      }
    }

    // 所有页签检查通过，基于最新状态原子移除 Workspace
    set((state) => {
      const nextCallbacks = { ...state.beforeCloseCallbacks };
      for (const tabKey of checkedKeys) {
        delete nextCallbacks[callbackKey(appNumber, tabKey)];
      }
      const latestWs = state.workspaces[appNumber];
      if (latestWs) {
        for (const tab of latestWs.contentTabs) {
          delete nextCallbacks[callbackKey(appNumber, tab.key)];
        }
      }
      const next = { ...state.workspaces };
      delete next[appNumber];
      return { workspaces: next, beforeCloseCallbacks: nextCallbacks };
    });
    return true;
  },

  /** 批量关闭内容页签 — 顺序检查，全部通过后基于最新状态原子移除 */
  closeContentTabs: async (appNumber, tabKeys) => {
    // 第一阶段：顺序检查所有 beforeClose 回调（回调映射独立于 tab 列表，此处可安全快照）
    const { beforeCloseCallbacks } = get();
    for (const tabKey of tabKeys) {
      if (tabKey === '__home__') continue;
      const key = callbackKey(appNumber, tabKey);
      const beforeClose = beforeCloseCallbacks[key];
      if (beforeClose) {
        try {
          const canClose = await beforeClose();
          if (!canClose) return false;
        } catch {
          return false;
        }
      }
    }

    // 第二阶段：基于最新状态原子移除，避免覆盖检查期间的并发变更
    const tabKeySet = new Set(tabKeys);
    set((state) => {
      const ws = state.workspaces[appNumber];
      if (!ws) return state;

      const contentTabs = ws.contentTabs.filter((t) => !tabKeySet.has(t.key));
      const nextCallbacks = { ...state.beforeCloseCallbacks };
      for (const tabKey of tabKeys) {
        delete nextCallbacks[callbackKey(appNumber, tabKey)];
      }

      let activeContentTabKey = ws.activeContentTabKey;
      if (tabKeySet.has(activeContentTabKey)) {
        const remainingKeys = new Set(contentTabs.map((tab) => tab.key));
        activeContentTabKey = resolveNextActiveTabKey(
          ws.activeContentTabHistory,
          remainingKeys,
          '__home__',
          tabKeySet,
        );
      }

      const activeContentTabHistory = pushTabHistory(
        ws.activeContentTabHistory.filter((key) => !tabKeySet.has(key)),
        activeContentTabKey,
      );

      return {
        workspaces: {
          ...state.workspaces,
          [appNumber]: { ...ws, contentTabs, activeContentTabKey, activeContentTabHistory },
        },
        beforeCloseCallbacks: nextCallbacks,
      };
    });
    return true;
  },

  /** 检查所有 Workspace 中是否有未保存数据（退出登录等全局操作前调用） */
  checkAllDirty: async () => {
    const { workspaces, beforeCloseCallbacks } = get();
    for (const [appNumber, ws] of Object.entries(workspaces)) {
      for (const tab of ws.contentTabs) {
        if (tab.key === '__home__') continue;
        const key = callbackKey(appNumber, tab.key);
        const beforeClose = beforeCloseCallbacks[key];
        if (beforeClose) {
          try {
            const canClose = await beforeClose();
            if (!canClose) return false;
          } catch {
            return false;
          }
        }
      }
    }
    return true;
  },

  registerBeforeClose: (appNumber, tabKey, fn) => {
    set((state) => ({
      beforeCloseCallbacks: {
        ...state.beforeCloseCallbacks,
        [callbackKey(appNumber, tabKey)]: fn,
      },
    }));
  },

  unregisterBeforeClose: (appNumber, tabKey) => {
    set((state) => {
      const next = { ...state.beforeCloseCallbacks };
      delete next[callbackKey(appNumber, tabKey)];
      return { beforeCloseCallbacks: next };
    });
  },

  addContentTab: (appNumber, tab) => {
    const { workspaces } = get();
    const ws = workspaces[appNumber];
    // Workspace 未初始化说明调用方逻辑错误，架构阶段直接抛异常暴露问题
    if (!ws) {
      throw new Error(`[workbench] Workspace "${appNumber}" 不存在，请先调用 initWorkspace。`);
    }

    const exists = ws.contentTabs.find((t) => t.key === tab.key);
    if (exists) {
      set({
        workspaces: {
          ...workspaces,
          [appNumber]: {
            ...ws,
            activeContentTabKey: tab.key,
            activeContentTabHistory: pushTabHistory(ws.activeContentTabHistory, tab.key),
          },
        },
      });
      return 'activated';
    }

    // 页签数量限制（不包含首页占位）
    const nonHomeTabs = ws.contentTabs.filter((t) => t.key !== '__home__');
    if (nonHomeTabs.length >= MAX_CONTENT_TABS) {
      return 'limit_reached';
    }

    set({
      workspaces: {
        ...workspaces,
        [appNumber]: {
          ...ws,
          contentTabs: [...ws.contentTabs, tab],
          activeContentTabKey: tab.key,
          activeContentTabHistory: pushTabHistory(ws.activeContentTabHistory, tab.key),
        },
      },
    });
    return 'opened';
  },

  openListTab: (appNumber, componentKey, label) => {
    return get().addContentTab(appNumber, {
      key: createListTabKey(componentKey),
      label,
      closable: true,
      componentKey,
      pageType: 'LIST',
    });
  },

  openAddNewTab: (appNumber, componentKey, label) => {
    return get().addContentTab(appNumber, {
      key: createAddNewTabKey(componentKey),
      label,
      closable: true,
      componentKey,
      pageType: 'EDIT',
      operationType: OperationType.ADDNEW,
      temporary: true,
    });
  },

  openBillTab: (appNumber, componentKey, label, billId, operationType) => {
    return get().addContentTab(appNumber, {
      key: createBillTabKey(componentKey, billId),
      label,
      closable: true,
      componentKey,
      pageType: 'EDIT',
      operationType,
      billId,
    });
  },

  replaceContentTab: (appNumber, oldTabKey, nextTab) => {
    const { workspaces, beforeCloseCallbacks } = get();
    const ws = workspaces[appNumber];
    if (!ws) return;

    // 迁移 beforeClose 回调：oldKey → newKey
    const oldCbKey = callbackKey(appNumber, oldTabKey);
    const newCbKey = callbackKey(appNumber, nextTab.key);
    const nextCallbacks = { ...beforeCloseCallbacks };
    if (nextCallbacks[oldCbKey]) {
      nextCallbacks[newCbKey] = nextCallbacks[oldCbKey];
      delete nextCallbacks[oldCbKey];
    }

    const contentTabs = ws.contentTabs.map((tab) => (tab.key === oldTabKey ? nextTab : tab));
    const activeContentTabKey =
      ws.activeContentTabKey === oldTabKey ? nextTab.key : ws.activeContentTabKey;
    const activeContentTabHistory = pushTabHistory(
      ws.activeContentTabHistory.filter((key) => key !== oldTabKey),
      activeContentTabKey,
    );
    set({
      workspaces: {
        ...workspaces,
        [appNumber]: { ...ws, contentTabs, activeContentTabKey, activeContentTabHistory },
      },
      beforeCloseCallbacks: nextCallbacks,
    });
  },

  removeContentTab: async (appNumber, tabKey) => {
    if (tabKey === '__home__') return;

    // 检查关闭前回调
    const { beforeCloseCallbacks } = get();
    const beforeClose = beforeCloseCallbacks[callbackKey(appNumber, tabKey)];
    if (beforeClose) {
      try {
        const canClose = await beforeClose();
        if (!canClose) return;
      } catch {
        return;
      }
    }

    const { workspaces } = get();
    const ws = workspaces[appNumber];
    if (!ws) return;
    const newTabs = ws.contentTabs.filter((t) => t.key !== tabKey);
    const remainingKeys = new Set(newTabs.map((tab) => tab.key));
    const nextHistory = ws.activeContentTabHistory.filter((key) => key !== tabKey);
    let newActiveKey = ws.activeContentTabKey;
    if (ws.activeContentTabKey === tabKey) {
      newActiveKey = resolveNextActiveTabKey(
        ws.activeContentTabHistory,
        remainingKeys,
        '__home__',
        new Set([tabKey]),
      );
    }

    const nextCallbacks = { ...beforeCloseCallbacks };
    delete nextCallbacks[callbackKey(appNumber, tabKey)];

    set({
      workspaces: {
        ...workspaces,
        [appNumber]: {
          ...ws,
          contentTabs: newTabs,
          activeContentTabKey: newActiveKey,
          activeContentTabHistory: pushTabHistory(nextHistory, newActiveKey),
        },
      },
      beforeCloseCallbacks: nextCallbacks,
    });
  },

  activateContentTab: (appNumber, tabKey) => {
    const { workspaces } = get();
    const ws = workspaces[appNumber];
    if (!ws) return;
    set({
      workspaces: {
        ...workspaces,
        [appNumber]: {
          ...ws,
          activeContentTabKey: tabKey,
          activeContentTabHistory: pushTabHistory(ws.activeContentTabHistory, tabKey),
        },
      },
    });
  },
}));
