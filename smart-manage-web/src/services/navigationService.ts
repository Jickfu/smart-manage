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

/** 请求序号 — 每次 openApp 递增，防止异步竞态导致旧数据覆盖新状态 */
let requestSeq = 0;

/**
 * 统一的异步应用打开服务。
 *
 * 负责：查询应用信息 → 创建 Workspace → 添加 Header Tab → 激活。
 * 内置请求序号防止快速切换时的异步竞态。
 * 应用不存在或请求失败时自动回退到 apps。
 */
export async function openApp(appNumber: string): Promise<void> {
  const seq = ++requestSeq;

  // 内置应用：直接激活
  if (appNumber === 'home' || appNumber === 'apps') {
    useHeaderTabsStore.getState().activate(appNumber);
    return;
  }

  // 已打开的 Workspace 直接激活，不重复请求
  const existingWs = useWorkbenchStore.getState().workspaces[appNumber];
  const existingTab = useHeaderTabsStore.getState().tabs.find((t) => t.key === appNumber);
  if (existingWs && existingTab) {
    useHeaderTabsStore.getState().activate(appNumber);
    return;
  }

  try {
    const appInfo = await openByNumber(appNumber);
    if (seq !== requestSeq) return;

    const headerStore = useHeaderTabsStore.getState();
    const workbenchStore = useWorkbenchStore.getState();

    workbenchStore.initWorkspace(appNumber, appInfo);
    headerStore.addAppTab(appNumber, appInfo.name);
  } catch {
    if (seq !== requestSeq) return;
    // 应用不存在或无权访问，回退到应用列表
    useHeaderTabsStore.getState().activate('apps');
  }
}

/**
 * 移除应用 — 先检查 Workspace 脏数据，通过后移除。
 * 返回 true 表示已关闭。
 */
export async function closeAppAndRemove(appNumber: string): Promise<boolean> {
  const target = useHeaderTabsStore.getState().tabs.find((tab) => tab.key === appNumber);
  if (target?.pinned) return false;
  const allowed = await useWorkbenchStore.getState().closeWorkspace(appNumber);
  if (!allowed) return false;

  useWorkbenchStore.getState().destroyWorkspace(appNumber);
  useHeaderTabsStore.getState().removeAppTab(appNumber);
  return true;
}
