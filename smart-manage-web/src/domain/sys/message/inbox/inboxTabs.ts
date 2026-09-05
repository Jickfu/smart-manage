import type { InboxReceiptKey } from './types';
import { inboxReceiptId } from './inboxPresentation';

export const inboxCategoryLabels: Record<string, string> = {
  'messages-all': '全部消息',
  'messages-announcement': '系统公告',
  'messages-business': '业务通知',
  'task-pending': '待处理',
  'task-completed': '已处理',
  'task-started': '我发起的',
};
export interface InboxTab {
  key: string;
  label: string;
  closable: boolean;
  category: string;
  receipt?: InboxReceiptKey;
}
export interface InboxTabsState {
  tabs: InboxTab[];
  activeKey: string;
  revision: number;
  blockedNavigation?: boolean;
}
export const inboxDetailTabKey = (receipt: InboxReceiptKey) => `detail:${inboxReceiptId(receipt)}`;
export const inboxListTab = (category: string): InboxTab => ({
  key: '__home__',
  category,
  label: '消息列表',
  closable: false,
});
export function openInboxTab(state: InboxTabsState, tab: InboxTab): InboxTabsState {
  const exists = state.tabs.some((item) => item.key === tab.key);
  return {
    ...state,
    blockedNavigation: false,
    activeKey: tab.key,
    tabs: exists
      ? state.tabs.map((item) => (item.key === '__home__' && tab.key === '__home__' ? tab : item))
      : [...state.tabs, tab],
  };
}
export function closeInboxTabs(state: InboxTabsState, keys: string[]): InboxTabsState {
  const tabs = state.tabs.filter((tab) => !tab.closable || !keys.includes(tab.key));
  const activeKey = tabs.some((tab) => tab.key === state.activeKey) ? state.activeKey : '__home__';
  return { ...state, tabs, activeKey };
}
/** 预览导航只激活或追加目标，不重建已有列表和详情。 */
export function navigateInbox(
  state: InboxTabsState,
  section: 'messages' | 'tasks',
  receipt: InboxReceiptKey | undefined,
  revision: number,
) {
  const category = section === 'tasks' ? 'task-pending' : 'messages-all';
  const next = receipt
    ? { ...state, revision }
    : openInboxTab({ ...state, revision }, inboxListTab(category));
  return receipt
    ? openInboxTab(next, {
        key: inboxDetailTabKey(receipt),
        label: '消息详情',
        closable: true,
        category,
        receipt,
      })
    : next;
}
