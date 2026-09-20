/**
 * 导航服务 — 应用切换的唯一入口，统一控制 Store 操作。
 *
 * 架构约定：
 * - Store（headerTabs/workbench）只做纯状态变更
 * - navigationService 是唯一的应用打开/关闭入口
 * - URL 固定不变，应用状态由 Zustand 内存管理
 */

import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useWorkbenchStore } from '@/stores/workbench';
import { openByNumber } from '@/domain/sys/base/app/api';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { createExternalLinkTabKey, createListTabKey } from '@/domain/common/page/tab/tabKeys';
import type { MenuVO } from '@/types/api';
import { resolveMenuAction } from '@/pages/workbench/menuNavigation';
import type { MenuAction } from '@/pages/workbench/menuNavigation';
import type { ContentTabItem } from '@/stores/workbench';

/** 请求序号 — 每次 openApp 递增，防止异步竞态导致旧数据覆盖新状态 */
let requestSeq = 0;

export type OpenAppResult =
  | { status: 'opened' | 'activated' }
  | { status: 'capacity-exceeded' }
  | { status: 'superseded' }
  | { status: 'failed'; error: unknown };

export type OpenMenuItemResult =
  | OpenAppResult
  | { status: 'menu-opened' }
  | { status: 'external-opened' };

function createMenuTargetTab(action: MenuAction): ContentTabItem | null {
  if (action.type === 'EXTERNAL_NEW_TAB') return null;
  if (action.type === 'EXTERNAL_IFRAME') {
    return {
      key: createExternalLinkTabKey(action.menuId),
      label: action.title,
      closable: true,
      externalUrl: action.externalUrl,
    };
  }
  const pageType =
    componentRegistry[action.componentKey]?.pageType === 'CUSTOM' ? 'CUSTOM' : 'LIST';
  return {
    key: createListTabKey(action.componentKey),
    label: getRegisteredTabTitle(action.componentKey, pageType),
    closable: true,
    componentKey: action.componentKey,
    pageType,
  };
}

/**
 * 统一的异步应用打开服务。
 *
 * 负责：查询应用信息 → 创建 Workspace → 添加 Header Tab → 激活。
 * 内置请求序号防止快速切换时的异步竞态。
 * 启动调用方可以要求失败时回退 apps；用户主动导航默认保留当前有效页面。
 */
export async function openApp(appNumber: string, fallbackToApps = false): Promise<OpenAppResult> {
  const seq = ++requestSeq;

  if (appNumber === 'builtin:inbox') {
    const store = useHeaderTabsStore.getState();
    if (store.tabs.some((tab) => tab.type === 'inbox' && tab.loaded)) store.activate(appNumber);
    else store.openInbox('messages');
    return { status: 'activated' };
  }

  // 内置应用：直接激活
  if (appNumber === 'home' || appNumber === 'apps') {
    useHeaderTabsStore.getState().activate(appNumber);
    return { status: 'activated' };
  }

  // 已打开的 Workspace 直接激活，不重复请求
  const existingWs = useWorkbenchStore.getState().workspaces[appNumber];
  const existingTab = useHeaderTabsStore.getState().tabs.find((t) => t.key === appNumber);
  if (existingWs && existingTab) {
    useHeaderTabsStore.getState().activate(appNumber);
    return { status: 'activated' };
  }

  try {
    const appInfo = await openByNumber(appNumber);
    if (seq !== requestSeq) return { status: 'superseded' };

    const headerStore = useHeaderTabsStore.getState();
    const workbenchStore = useWorkbenchStore.getState();

    const workspaceResult = workbenchStore.initWorkspace(appNumber, appInfo);
    if (workspaceResult === 'capacity-exceeded') return { status: 'capacity-exceeded' };
    headerStore.addAppTab(appNumber, appInfo.name);
    return { status: 'opened' };
  } catch (error) {
    if (seq !== requestSeq) return { status: 'superseded' };
    if (fallbackToApps) useHeaderTabsStore.getState().activate('apps');
    return { status: 'failed', error };
  }
}

/** 从已授权菜单入口打开页面，首页快捷入口与侧边栏共享同一目标解析规则。 */
export async function openMenuItem(appNumber: string, item: MenuVO): Promise<OpenMenuItemResult> {
  const seq = ++requestSeq;
  const action = resolveMenuAction(item);
  const targetTab = createMenuTargetTab(action);
  const currentWorkspace = useWorkbenchStore.getState().workspaces[appNumber];
  let appInfo = currentWorkspace?.appInfo;
  if (!appInfo) {
    try {
      appInfo = await openByNumber(appNumber);
    } catch (error) {
      return seq === requestSeq ? { status: 'failed', error } : { status: 'superseded' };
    }
  }
  if (seq !== requestSeq) return { status: 'superseded' };

  const targetResult = useWorkbenchStore.getState().openMenuTarget(appNumber, appInfo, targetTab);
  if (targetResult === 'capacity-exceeded') return { status: 'capacity-exceeded' };
  useHeaderTabsStore.getState().addAppTab(appNumber, appInfo.name);

  if (action.type === 'EXTERNAL_NEW_TAB') {
    window.open(action.externalUrl, '_blank', 'noopener,noreferrer');
    return { status: 'external-opened' };
  }
  return { status: 'menu-opened' };
}

/**
 * 移除应用 — 先检查 Workspace 脏数据，通过后移除。
 * 返回 true 表示已关闭。
 */
export async function closeAppAndRemove(appNumber: string): Promise<boolean> {
  const target = useHeaderTabsStore.getState().tabs.find((tab) => tab.key === appNumber);
  if (target?.pinned) return false;
  if (target?.type === 'inbox') {
    useHeaderTabsStore.getState().removeAppTab(appNumber);
    return true;
  }
  const allowed = await useWorkbenchStore.getState().closeWorkspace(appNumber);
  if (!allowed) return false;

  useWorkbenchStore.getState().destroyWorkspace(appNumber);
  useHeaderTabsStore.getState().removeAppTab(appNumber);
  return true;
}

/** 全局消息入口不要求消息服务应用权限，也不创建业务应用工作台。 */
export function openInboxCenter(
  section: 'messages' | 'tasks' = 'messages',
  receipt?: { messageId: string; receivedTime: string },
): void {
  ++requestSeq;
  useHeaderTabsStore.getState().openInbox(section, receipt);
}
