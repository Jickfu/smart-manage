import { create } from 'zustand';
import {
  createAddNewTabKey,
  createBillTabKey,
  createExternalLinkTabKey,
  createListTabKey,
} from '@/domain/common/page/tab/tabKeys';
import { OperationType } from '@/domain/common/page/types';
import type { PageType } from '@/domain/common/page/types';
import type { AppVO } from '@/domain/sys/base/app/types';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { pushTabHistory, resolveNextActiveTabKey } from './tabHistory';

/** addContentTab 返回结果 */
export const RETAINED_PAGE_WARNING_THRESHOLD = 40;
export const RETAINED_PAGE_LIMIT = 50;

export const retainedPageWarningMessage = () =>
  `已保留 ${RETAINED_PAGE_WARNING_THRESHOLD} 个页面；达到 ${RETAINED_PAGE_LIMIT} 个后将不再打开新页面`;
export const retainedPageLimitMessage = () =>
  `已达到 ${RETAINED_PAGE_LIMIT} 个页面的保留上限，请关闭部分页面后再打开`;

export type AddTabResult = 'opened' | 'activated' | 'capacity-exceeded';
export type InitWorkspaceResult = 'initialized' | 'existing' | 'capacity-exceeded';

export interface CapacityNotice {
  revision: number;
  appNumber: string;
  type: 'warning';
  retainedPageCount: number;
}

export interface ContentTabItem {
  key: string;
  label: string;
  closable: boolean;
  componentKey?: string;
  pageType?: 'LIST' | 'EDIT' | 'CUSTOM';
  operationType?: OperationType;
  billId?: string;
  context?: Record<string, string>;
  /** 新增页临时标记 — 保存后替换为真实单据 tab */
  temporary?: boolean;
  /** iframe 外链不经过前端组件注册表，直接由工作台受控渲染。 */
  externalUrl?: string;
}

type ReplaceContentTabItem = Omit<ContentTabItem, 'label' | 'componentKey' | 'pageType'> & {
  componentKey: string;
  pageType: PageType;
};

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
  /** 页面渲染失败后由页签外壳持有的保守关闭确认，不与子页面守卫共用生命周期。 */
  failedCloseCallbacks: Record<string, BeforeCloseFn>;
  capacityNotice?: CapacityNotice;
  consumeCapacityNotice: (revision: number) => void;
  initWorkspace: (appNumber: string, appInfo: AppVO) => InitWorkspaceResult;
  destroyWorkspace: (appNumber: string) => void;
  /** 异步关闭 Workspace — 先顺序检查所有内容页 beforeClose，全部通过后一次性销毁 */
  closeWorkspace: (appNumber: string) => Promise<boolean>;
  /** 异步批量关闭内容页签 — 顺序检查 beforeClose，全部通过后原子移除 */
  closeContentTabs: (appNumber: string, tabKeys: string[]) => Promise<boolean>;
  /** 检查所有 Workspace 是否有未保存数据（供退出登录等全局操作使用） */
  checkAllDirty: () => Promise<boolean>;
  /** 仅检查仍存活页面的正常关闭守卫，供整页刷新在最终故障风险确认前调用。 */
  checkAllBeforeClose: () => Promise<boolean>;
  /** 页面组件注册关闭前检查回调 */
  registerBeforeClose: (appNumber: string, tabKey: string, fn: BeforeCloseFn) => void;
  /** 页面组件注销关闭前检查回调 */
  unregisterBeforeClose: (appNumber: string, tabKey: string) => void;
  registerFailedClose: (appNumber: string, tabKey: string, fn: BeforeCloseFn) => void;
  unregisterFailedClose: (appNumber: string, tabKey: string) => void;
  /** 添加/激活 content tab — 返回操作结果供调用层反馈 */
  addContentTab: (appNumber: string, tab: ContentTabItem) => AddTabResult;
  /** 原子提交组合菜单导航，避免先创建应用首页、后因内容页超限留下半完成状态。 */
  openMenuTarget: (appNumber: string, appInfo: AppVO, tab: ContentTabItem | null) => AddTabResult;
  openListTab: (appNumber: string, componentKey: string) => AddTabResult;
  openCustomTab: (appNumber: string, componentKey: string) => AddTabResult;
  openExternalLinkTab: (
    appNumber: string,
    menuId: string,
    title: string,
    externalUrl: string,
  ) => AddTabResult;
  openAddNewTab: (
    appNumber: string,
    componentKey: string,
    context?: Record<string, string>,
  ) => AddTabResult;
  openBillTab: (
    appNumber: string,
    componentKey: string,
    billId: string,
    operationType: OperationType,
  ) => AddTabResult;
  replaceContentTab: (appNumber: string, oldTabKey: string, nextTab: ReplaceContentTabItem) => void;
  updateContentTabLabel: (appNumber: string, tabKey: string, label: string) => void;
  removeContentTab: (appNumber: string, tabKey: string) => Promise<void>;
  activateContentTab: (appNumber: string, tabKey: string) => void;
}

/** 顺序执行最新的页面关闭守卫；任一页面拒绝或检查异常都终止整个关闭事务。 */
async function runNormalCloseGuards(
  getState: () => WorkbenchState,
  appNumber: string,
  tabKeys: string[],
): Promise<boolean> {
  for (const tabKey of tabKeys) {
    if (tabKey === '__home__') continue;
    const beforeClose = getState().beforeCloseCallbacks[callbackKey(appNumber, tabKey)];
    if (!beforeClose) continue;
    try {
      if (!(await beforeClose())) return false;
    } catch {
      return false;
    }
  }
  return true;
}

async function runFailedCloseGuards(
  getState: () => WorkbenchState,
  appNumber: string,
  tabKeys: string[],
): Promise<boolean> {
  for (const tabKey of tabKeys) {
    const failedClose = getState().failedCloseCallbacks[callbackKey(appNumber, tabKey)];
    if (!failedClose) continue;
    try {
      if (!(await failedClose())) return false;
    } catch {
      return false;
    }
  }
  return true;
}

async function runBeforeCloseGuards(
  getState: () => WorkbenchState,
  appNumber: string,
  tabKeys: string[],
): Promise<boolean> {
  return (
    (await runNormalCloseGuards(getState, appNumber, tabKeys)) &&
    (await runFailedCloseGuards(getState, appNumber, tabKeys))
  );
}

function defaultWorkspace(appInfo: AppVO): WorkspaceState {
  return {
    appInfo,
    contentTabs: [{ key: '__home__', label: '应用首页', closable: false }],
    activeContentTabKey: '__home__',
    activeContentTabHistory: ['__home__'],
  };
}

export function countRetainedPages(workspaces: Record<string, WorkspaceState>): number {
  return Object.values(workspaces).reduce(
    (count, workspace) => count + workspace.contentTabs.length,
    0,
  );
}

function nextCapacityNotice(
  current: CapacityNotice | undefined,
  type: CapacityNotice['type'],
  appNumber: string,
  retainedPageCount: number,
): CapacityNotice {
  return { revision: (current?.revision ?? 0) + 1, appNumber, type, retainedPageCount };
}

export const useWorkbenchStore = create<WorkbenchState>((set, get) => ({
  workspaces: {},
  beforeCloseCallbacks: {},
  failedCloseCallbacks: {},
  capacityNotice: undefined,

  consumeCapacityNotice: (revision) => {
    set((state) =>
      state.capacityNotice?.revision === revision ? { capacityNotice: undefined } : state,
    );
  },

  initWorkspace: (appNumber, appInfo) => {
    const { workspaces, capacityNotice } = get();
    if (workspaces[appNumber]) return 'existing';
    const retainedPageCount = countRetainedPages(workspaces);
    if (retainedPageCount >= RETAINED_PAGE_LIMIT) {
      return 'capacity-exceeded';
    }
    const nextRetainedPageCount = retainedPageCount + 1;
    set({
      workspaces: { ...workspaces, [appNumber]: defaultWorkspace(appInfo) },
      ...(nextRetainedPageCount === RETAINED_PAGE_WARNING_THRESHOLD
        ? {
            capacityNotice: nextCapacityNotice(
              capacityNotice,
              'warning',
              appNumber,
              nextRetainedPageCount,
            ),
          }
        : {}),
    });
    return 'initialized';
  },

  destroyWorkspace: (appNumber) => {
    const { workspaces, beforeCloseCallbacks, failedCloseCallbacks } = get();
    if (!workspaces[appNumber]) return;
    const ws = workspaces[appNumber];
    const nextCallbacks = { ...beforeCloseCallbacks };
    const nextFailedCallbacks = { ...failedCloseCallbacks };
    for (const tab of ws.contentTabs) {
      delete nextCallbacks[callbackKey(appNumber, tab.key)];
      delete nextFailedCallbacks[callbackKey(appNumber, tab.key)];
    }
    const next = { ...workspaces };
    delete next[appNumber];
    set({
      workspaces: next,
      beforeCloseCallbacks: nextCallbacks,
      failedCloseCallbacks: nextFailedCallbacks,
    });
  },

  closeWorkspace: async (appNumber) => {
    const ws = get().workspaces[appNumber];
    if (!ws) return true;

    // 关闭守卫等待期间可能新增页签，持续收敛到一个稳定快照后再原子销毁工作区。
    const checkedKeys = new Set<string>();
    while (true) {
      const currentWs = get().workspaces[appNumber];
      if (!currentWs) return true;
      const unchecked = currentWs.contentTabs.filter((t) => !checkedKeys.has(t.key));
      if (unchecked.length === 0) break;
      const uncheckedKeys = unchecked.map((tab) => tab.key);
      if (!(await runBeforeCloseGuards(get, appNumber, uncheckedKeys))) return false;
      uncheckedKeys.forEach((tabKey) => checkedKeys.add(tabKey));
    }

    // 所有页签检查通过，基于最新状态原子移除 Workspace
    set((state) => {
      const nextCallbacks = { ...state.beforeCloseCallbacks };
      const nextFailedCallbacks = { ...state.failedCloseCallbacks };
      for (const tabKey of checkedKeys) {
        delete nextCallbacks[callbackKey(appNumber, tabKey)];
        delete nextFailedCallbacks[callbackKey(appNumber, tabKey)];
      }
      const latestWs = state.workspaces[appNumber];
      if (latestWs) {
        for (const tab of latestWs.contentTabs) {
          delete nextCallbacks[callbackKey(appNumber, tab.key)];
          delete nextFailedCallbacks[callbackKey(appNumber, tab.key)];
        }
      }
      const next = { ...state.workspaces };
      delete next[appNumber];
      return {
        workspaces: next,
        beforeCloseCallbacks: nextCallbacks,
        failedCloseCallbacks: nextFailedCallbacks,
      };
    });
    return true;
  },

  /** 批量关闭内容页签 — 顺序检查，全部通过后基于最新状态原子移除 */
  closeContentTabs: async (appNumber, tabKeys) => {
    if (!(await runBeforeCloseGuards(get, appNumber, tabKeys))) return false;

    // 第二阶段：基于最新状态原子移除，避免覆盖检查期间的并发变更
    const tabKeySet = new Set(tabKeys);
    set((state) => {
      const ws = state.workspaces[appNumber];
      if (!ws) return state;

      const contentTabs = ws.contentTabs.filter((t) => !tabKeySet.has(t.key));
      const nextCallbacks = { ...state.beforeCloseCallbacks };
      const nextFailedCallbacks = { ...state.failedCloseCallbacks };
      for (const tabKey of tabKeys) {
        delete nextCallbacks[callbackKey(appNumber, tabKey)];
        delete nextFailedCallbacks[callbackKey(appNumber, tabKey)];
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
        failedCloseCallbacks: nextFailedCallbacks,
      };
    });
    return true;
  },

  /** 检查所有 Workspace 中是否有未保存数据（退出登录等全局操作前调用） */
  checkAllDirty: async () => {
    const { workspaces } = get();
    for (const [appNumber, ws] of Object.entries(workspaces)) {
      const tabKeys = ws.contentTabs.map((tab) => tab.key);
      if (!(await runNormalCloseGuards(get, appNumber, tabKeys))) return false;
    }
    for (const [appNumber, ws] of Object.entries(get().workspaces)) {
      const tabKeys = ws.contentTabs.map((tab) => tab.key);
      if (!(await runFailedCloseGuards(get, appNumber, tabKeys))) return false;
    }
    return true;
  },

  checkAllBeforeClose: async () => {
    for (const [appNumber, ws] of Object.entries(get().workspaces)) {
      const tabKeys = ws.contentTabs.map((tab) => tab.key);
      if (!(await runNormalCloseGuards(get, appNumber, tabKeys))) return false;
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

  registerFailedClose: (appNumber, tabKey, fn) => {
    set((state) => ({
      failedCloseCallbacks: {
        ...state.failedCloseCallbacks,
        [callbackKey(appNumber, tabKey)]: fn,
      },
    }));
  },

  unregisterFailedClose: (appNumber, tabKey) => {
    set((state) => {
      const next = { ...state.failedCloseCallbacks };
      delete next[callbackKey(appNumber, tabKey)];
      return { failedCloseCallbacks: next };
    });
  },

  openMenuTarget: (appNumber, appInfo, tab) => {
    const { workspaces, capacityNotice } = get();
    const currentWorkspace = workspaces[appNumber];
    const existingTab = tab
      ? currentWorkspace?.contentTabs.find((item) => item.key === tab.key)
      : undefined;
    const additionalPages = (currentWorkspace ? 0 : 1) + (tab && !existingTab ? 1 : 0);
    const retainedPageCount = countRetainedPages(workspaces);
    if (retainedPageCount + additionalPages > RETAINED_PAGE_LIMIT) {
      return 'capacity-exceeded';
    }

    const workspace = currentWorkspace ?? defaultWorkspace(appInfo);
    const nextWorkspace = existingTab
      ? {
          ...workspace,
          activeContentTabKey: existingTab.key,
          activeContentTabHistory: pushTabHistory(
            workspace.activeContentTabHistory,
            existingTab.key,
          ),
        }
      : tab
        ? {
            ...workspace,
            contentTabs: [...workspace.contentTabs, tab],
            activeContentTabKey: tab.key,
            activeContentTabHistory: pushTabHistory(workspace.activeContentTabHistory, tab.key),
          }
        : workspace;
    const nextRetainedPageCount = retainedPageCount + additionalPages;
    set({
      workspaces: { ...workspaces, [appNumber]: nextWorkspace },
      ...(retainedPageCount < RETAINED_PAGE_WARNING_THRESHOLD &&
      nextRetainedPageCount >= RETAINED_PAGE_WARNING_THRESHOLD
        ? {
            capacityNotice: nextCapacityNotice(
              capacityNotice,
              'warning',
              appNumber,
              nextRetainedPageCount,
            ),
          }
        : {}),
    });
    return existingTab || (currentWorkspace && !tab) ? 'activated' : 'opened';
  },

  addContentTab: (appNumber, tab) => {
    const { workspaces, capacityNotice } = get();
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

    const retainedPageCount = countRetainedPages(workspaces);
    if (retainedPageCount >= RETAINED_PAGE_LIMIT) {
      return 'capacity-exceeded';
    }

    const nextRetainedPageCount = retainedPageCount + 1;
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
      ...(nextRetainedPageCount === RETAINED_PAGE_WARNING_THRESHOLD
        ? {
            capacityNotice: nextCapacityNotice(
              capacityNotice,
              'warning',
              appNumber,
              nextRetainedPageCount,
            ),
          }
        : {}),
    });
    return 'opened';
  },

  openListTab: (appNumber, componentKey) => {
    return get().addContentTab(appNumber, {
      key: createListTabKey(componentKey),
      label: getRegisteredTabTitle(componentKey, 'LIST'),
      closable: true,
      componentKey,
      pageType: 'LIST',
    });
  },

  openCustomTab: (appNumber, componentKey) => {
    return get().addContentTab(appNumber, {
      key: createListTabKey(componentKey),
      label: getRegisteredTabTitle(componentKey, 'CUSTOM'),
      closable: true,
      componentKey,
      pageType: 'CUSTOM',
    });
  },

  openExternalLinkTab: (appNumber, menuId, title, externalUrl) => {
    return get().addContentTab(appNumber, {
      key: createExternalLinkTabKey(menuId),
      label: title,
      closable: true,
      externalUrl,
    });
  },

  openAddNewTab: (appNumber, componentKey, context) => {
    return get().addContentTab(appNumber, {
      key: createAddNewTabKey(componentKey),
      label: getRegisteredTabTitle(componentKey, 'EDIT'),
      closable: true,
      componentKey,
      pageType: 'EDIT',
      operationType: OperationType.ADDNEW,
      temporary: true,
      context,
    });
  },

  openBillTab: (appNumber, componentKey, billId, operationType) => {
    return get().addContentTab(appNumber, {
      key: createBillTabKey(componentKey, billId),
      label: getRegisteredTabTitle(componentKey, 'EDIT'),
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

    const replacementTab: ContentTabItem = {
      ...nextTab,
      label: getRegisteredTabTitle(nextTab.componentKey, nextTab.pageType),
    };
    const contentTabs = ws.contentTabs.map((tab) => (tab.key === oldTabKey ? replacementTab : tab));
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

  updateContentTabLabel: (appNumber, tabKey, label) => {
    const normalizedLabel = label.trim();
    if (!normalizedLabel) {
      throw new Error('[workbench] 页签标题不能为空。');
    }
    set((state) => {
      const ws = state.workspaces[appNumber];
      if (!ws) return state;
      const targetTab = ws.contentTabs.find((tab) => tab.key === tabKey);
      if (!targetTab || targetTab.label === normalizedLabel) return state;
      return {
        workspaces: {
          ...state.workspaces,
          [appNumber]: {
            ...ws,
            contentTabs: ws.contentTabs.map((tab) =>
              tab.key === tabKey ? { ...tab, label: normalizedLabel } : tab,
            ),
          },
        },
      };
    });
  },

  removeContentTab: async (appNumber, tabKey) => {
    if (tabKey === '__home__') return;
    // 单页签与批量关闭共享同一套“等待守卫后基于最新状态提交”语义，避免旧快照覆盖并发注册的守卫。
    await get().closeContentTabs(appNumber, [tabKey]);
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
