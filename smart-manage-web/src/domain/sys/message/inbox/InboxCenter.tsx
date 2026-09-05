import { useCallback, useState } from 'react';
import { Empty } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import ContentTabsBarView from '@/pages/workbench/ContentTabsBarView';
import { inboxApi } from './api';
import { inboxQueryKeys } from './queryKeys';
import InboxDetailView from './InboxDetailView';
import InboxMessageList from './InboxMessageList';
import {
  closeInboxTabs,
  inboxCategoryLabels,
  inboxDetailTabKey,
  inboxListTab,
  navigateInbox,
  openInboxTab,
} from './inboxTabs';
import type { InboxTabsState } from './inboxTabs';
import type { InboxReceiptKey } from './types';
import './InboxCenter.css';

const MAX_DETAIL_TABS = 20;

export default function InboxCenter({
  initialSection,
  initialReceipt,
  navigationRevision = 0,
}: {
  initialSection: 'messages' | 'tasks';
  initialReceipt?: InboxReceiptKey;
  navigationRevision?: number;
}) {
  const active = useHeaderTabsStore((state) => state.activeKey === 'builtin:inbox');
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [state, setState] = useState<InboxTabsState>(() =>
    navigateInbox(
      {
        tabs: [inboxListTab('messages-all')],
        activeKey: '__home__',
        revision: navigationRevision,
      },
      initialSection,
      initialReceipt,
      navigationRevision,
    ),
  );
  // 同一Header入口的新导航只调整目标；不能通过组件key销毁整个消息中心。
  if (state.revision !== navigationRevision) {
    const targetKey = initialReceipt ? inboxDetailTabKey(initialReceipt) : undefined;
    const atCapacity =
      initialReceipt &&
      !state.tabs.some((tab) => tab.key === targetKey) &&
      state.tabs.filter((tab) => tab.receipt).length >= MAX_DETAIL_TABS;
    setState(
      atCapacity
        ? { ...state, revision: navigationRevision, blockedNavigation: true }
        : navigateInbox(state, initialSection, initialReceipt, navigationRevision),
    );
  }
  const category = state.tabs.find((tab) => tab.key === '__home__')!.category;
  const tasks = category.startsWith('task-');
  const unreadQuery = useQuery({
    queryKey: inboxQueryKeys.unread(),
    queryFn: inboxApi.unreadSummary,
    enabled: active,
  });
  const summary = unreadQuery.data;
  const categories = tasks
    ? [
        { key: 'task-pending', count: undefined },
        { key: 'task-completed', count: undefined },
        { key: 'task-started', count: undefined },
      ]
    : [
        {
          key: 'messages-all',
          count: summary ? (summary.overflow ? 100 : summary.unreadCount) : undefined,
        },
        { key: 'messages-announcement', count: summary?.announcementUnreadCount },
        { key: 'messages-business', count: summary?.businessUnreadCount },
      ];
  const openList = (key: string) =>
    setState((previous) => openInboxTab(previous, inboxListTab(key)));
  const updateTitle = useCallback((key: string, title: string) => {
    setState((previous) =>
      previous.tabs.some((tab) => tab.key === key && tab.label !== title)
        ? {
            ...previous,
            tabs: previous.tabs.map((tab) => (tab.key === key ? { ...tab, label: title } : tab)),
          }
        : previous,
    );
  }, []);
  const closeTabs = (keys: string[]) => setState((previous) => closeInboxTabs(previous, keys));
  return (
    <div className="sm-inbox-center">
      <aside className="sm-inbox-navigation">
        <div className="sm-inbox-section-switch" role="group" aria-label="消息中心类型">
          <button type="button" aria-pressed={!tasks} onClick={() => openList('messages-all')}>
            消息
          </button>
          <button type="button" aria-pressed={tasks} onClick={() => openList('task-pending')}>
            任务
          </button>
        </div>
        <nav className="sm-inbox-categories" aria-label={tasks ? '任务分类' : '消息分类'}>
          {categories.map((item) => (
            <button
              key={item.key}
              type="button"
              aria-current={category === item.key ? 'page' : undefined}
              onClick={() => openList(item.key)}
            >
              <span>{inboxCategoryLabels[item.key]}</span>
              {item.count !== undefined && (
                <span
                  className="sm-inbox-category-count"
                  aria-label={`${item.count >= 100 ? '99+' : item.count} 条未读`}
                >
                  {item.count >= 100 ? '99+' : item.count}
                </span>
              )}
            </button>
          ))}
        </nav>
      </aside>
      <main className="sm-inbox-main">
        <ContentTabsBarView
          contentTabs={state.tabs}
          activeContentTabKey={state.activeKey}
          onActivate={(key) => setState((previous) => ({ ...previous, activeKey: key }))}
          onRemove={(key) => closeTabs([key])}
          onCloseOthers={() =>
            closeTabs(state.tabs.filter((tab) => tab.key !== state.activeKey).map((tab) => tab.key))
          }
          onCloseAll={() => {
            void confirmOperation({
              type: 'warning',
              title: '关闭全部页签',
              description: '关闭全部消息详情，返回消息列表。',
              confirmText: '确定',
              onConfirm: () => closeTabs(state.tabs.map((tab) => tab.key)),
            });
          }}
        />
        {state.blockedNavigation && (
          <div role="status" className="sm-inbox-filter-summary">
            最多打开20个消息详情页签，请先关闭部分页签后重新打开消息。
          </div>
        )}
        {state.tabs.map((tab) => (
          <section
            key={tab.key}
            className="sm-inbox-tab-panel"
            role="tabpanel"
            aria-label={tab.label}
            hidden={state.activeKey !== tab.key}
          >
            {tab.receipt ? (
              <InboxDetailView
                receipt={tab.receipt}
                active={active && state.activeKey === tab.key}
                onBack={() => setState((previous) => ({ ...previous, activeKey: '__home__' }))}
                tabKey={tab.key}
                onTitleChange={updateTitle}
              />
            ) : tab.category.startsWith('task-') ? (
              <div className="sm-inbox-empty">
                <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="工作流任务暂未开放" />
              </div>
            ) : (
              <InboxMessageList
                key={tab.category}
                category={tab.category}
                title={inboxCategoryLabels[tab.category]!}
                active={active && state.activeKey === tab.key}
                onOpen={(receipt, title) => {
                  const key = inboxDetailTabKey(receipt);
                  if (
                    !state.tabs.some((item) => item.key === key) &&
                    state.tabs.filter((item) => item.receipt).length >= MAX_DETAIL_TABS
                  ) {
                    feedback.warning('最多打开20个消息详情页签，请先关闭部分页签');
                    return;
                  }
                  setState((previous) =>
                    openInboxTab(previous, {
                      key,
                      label: title,
                      closable: true,
                      category: tab.category,
                      receipt,
                    }),
                  );
                }}
              />
            )}
          </section>
        ))}
      </main>
    </div>
  );
}
