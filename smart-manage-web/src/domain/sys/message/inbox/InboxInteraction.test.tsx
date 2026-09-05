// @vitest-environment jsdom
import { act, StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import InboxHeaderButton from './InboxHeaderButton';
import InboxPreviewDrawer from './InboxPreviewDrawer';
import InboxCenter from './InboxCenter';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import InboxDetailView from './InboxDetailView';
import { inboxQueryKeys } from './queryKeys';
import { ApiError } from '@/api/ApiError';

const mocks = vi.hoisted(() => ({
  unreadSummary: vi.fn(),
  list: vi.fn(),
  detail: vi.fn(),
  markRead: vi.fn(),
  markUnread: vi.fn(),
  feedback: { fromError: vi.fn(), warning: vi.fn() },
}));
vi.mock('./api', () => ({ inboxApi: mocks }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/component/useOperationConfirm', () => ({
  useOperationConfirm: () => vi.fn(),
}));
vi.mock('@/services/navigationService', () => ({ openInboxCenter: vi.fn() }));

beforeEach(() => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  Element.prototype.scrollIntoView = vi.fn();
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  const nativeGetComputedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) =>
    nativeGetComputedStyle(element),
  );
  vi.stubGlobal('matchMedia', () => ({
    matches: false,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
});
afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  vi.resetAllMocks();
});

it('真实查询按60秒轮询，服务端返回0后停止定时请求', async () => {
  vi.useFakeTimers();
  mocks.unreadSummary
    .mockResolvedValueOnce({ unreadCount: 2, overflow: false, pollingIntervalSeconds: 60 })
    .mockResolvedValue({ unreadCount: 3, overflow: false, pollingIntervalSeconds: 0 });
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <InboxHeaderButton />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await vi.advanceTimersByTimeAsync(10);
    });
    expect(mocks.unreadSummary).toHaveBeenCalledTimes(1);
    await act(async () => {
      await vi.advanceTimersByTimeAsync(59980);
    });
    expect(mocks.unreadSummary).toHaveBeenCalledTimes(1);
    await act(async () => {
      await vi.advanceTimersByTimeAsync(30);
    });
    expect(mocks.unreadSummary).toHaveBeenCalledTimes(2);
    await act(async () => {
      await vi.advanceTimersByTimeAsync(180000);
    });
    expect(mocks.unreadSummary).toHaveBeenCalledTimes(2);
    expect(mocks.list).not.toHaveBeenCalled();
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});

it('详情成功后原样提交收件键且StrictMode不重复标记，正文按纯文本呈现', async () => {
  const receipt = { messageId: '9007199254740993', receivedTime: '2026-09-01 12:00:00.123456' };
  const detail = {
    ...receipt,
    title: '通知',
    content: '<script>invalid</script>',
    level: 'NORMAL',
    readStatus: false,
  };
  mocks.detail.mockResolvedValue(detail);
  mocks.markRead.mockResolvedValue(undefined);
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  client.setQueryData(inboxQueryKeys.detail(receipt.messageId, receipt.receivedTime), detail);
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <StrictMode>
          <QueryClientProvider client={client}>
            <InboxDetailView receipt={receipt} active onBack={() => undefined} />
          </QueryClientProvider>
        </StrictMode>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10));
    });
    expect(mocks.markRead).toHaveBeenCalledTimes(1);
    expect(mocks.markRead).toHaveBeenCalledWith([receipt]);
    expect(container.textContent).toContain('<script>invalid</script>');
    expect(container.querySelector('script')).toBeNull();
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});

it('详情被拒绝时不发送已读命令', async () => {
  mocks.detail.mockRejectedValue(new ApiError({ source: 'HTTP', httpStatus: 403, message: '' }));
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <InboxDetailView
            receipt={{ messageId: '30', receivedTime: '2026-09-01 12:00:00.123456' }}
            active
            onBack={() => undefined}
          />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 10));
    });
    expect(mocks.markRead).not.toHaveBeenCalled();
    expect(container.textContent).toContain('加载失败');
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});

it('预览只查询未读消息，不显示最新条数说明，已读后刷新不再展示', async () => {
  const record = {
    messageId: '30',
    receivedTime: '2026-09-01 12:00:00.123456',
    title: '待阅读通知',
    summary: '通知摘要',
    readStatus: false,
  };
  mocks.list.mockResolvedValue({ records: [record], hasMore: false });
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <InboxPreviewDrawer open onClose={() => undefined} />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(mocks.list).toHaveBeenCalledWith({ pageSize: 10, unreadOnly: true, monthOnly: false });
    expect(container.textContent).toContain('待阅读通知');
    expect(container.textContent).not.toContain('最新 10 条消息');
    mocks.list.mockResolvedValue({ records: [], hasMore: false });
    await act(async () => {
      await client.invalidateQueries({ queryKey: inboxQueryKeys.all });
    });
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(container.textContent).not.toContain('待阅读通知');
    expect(container.textContent).toContain('暂无未读消息');
    expect(mocks.markRead).not.toHaveBeenCalled();
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});

it('中心平铺分类显示独立未读数，切换任务隐藏消息分类并停止列表请求', async () => {
  useHeaderTabsStore.setState({ activeKey: 'builtin:inbox' });
  mocks.unreadSummary.mockResolvedValue({
    unreadCount: 99,
    overflow: true,
    pollingIntervalSeconds: 0,
    announcementUnreadCount: 100,
    businessUnreadCount: 7,
  });
  mocks.list.mockResolvedValue({ records: [], hasMore: false });
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <InboxCenter initialSection="messages" />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(container.querySelector('[role="menu"]')).toBeNull();
    const categories = container.querySelector('nav')!;
    expect(categories.textContent).toContain('系统公告99+');
    expect(categories.textContent).toContain('业务通知7');
    const business = Array.from(categories.querySelectorAll('button')).find((button) =>
      button.textContent?.startsWith('业务通知'),
    )!;
    await act(async () => business.click());
    expect(mocks.list).toHaveBeenLastCalledWith(
      expect.objectContaining({
        filters: JSON.stringify([
          { field: 'readStatus', type: 'boolean', operator: 'IN', values: [false] },
          { field: 'receivedTime', type: 'date', operator: 'THIS_MONTH' },
        ]),
        audienceType: 'USERS',
      }),
    );
    expect(container.querySelectorAll('.sm-content-tab')).toHaveLength(1);
    const calls = mocks.list.mock.calls.length;
    const tasks = Array.from(container.querySelectorAll('button')).find(
      (button) => button.textContent === '任务',
    )!;
    await act(async () => tasks.click());
    expect(container.querySelector('nav')?.textContent).toBe('待处理已处理我发起的');
    expect(container.textContent).toContain('工作流任务暂未开放');
    expect(mocks.list).toHaveBeenCalledTimes(calls);
  } finally {
    await act(async () => root.unmount());
    client.clear();
    useHeaderTabsStore.setState({ activeKey: 'home' });
  }
});

it('标题打开独立页签，返回列表保留节点和滚动，预览导航不丢已有页签', async () => {
  useHeaderTabsStore.setState({ activeKey: 'builtin:inbox' });
  const receipt = { messageId: '30', receivedTime: '2026-09-01 12:00:00.123456' };
  const record = {
    ...receipt,
    title: '页签通知',
    summary: '仅展示的摘要',
    content: '完整正文',
    level: 'NORMAL',
    readStatus: true,
  };
  mocks.unreadSummary.mockResolvedValue({
    unreadCount: 0,
    overflow: false,
    pollingIntervalSeconds: 0,
    announcementUnreadCount: 0,
    businessUnreadCount: 0,
  });
  mocks.list.mockResolvedValue({ records: [record], hasMore: false });
  mocks.detail.mockResolvedValue(record);
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  const root = createRoot(container);
  const render = (revision: number, target?: typeof receipt) =>
    root.render(
      <QueryClientProvider client={client}>
        <InboxCenter
          initialSection="messages"
          initialReceipt={target}
          navigationRevision={revision}
        />
      </QueryClientProvider>,
    );
  try {
    await act(async () => render(0));
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    const list = container.querySelector<HTMLElement>('[role="tabpanel"]')!;
    const body = list.querySelector<HTMLElement>('.ant-table-body')!;
    body.scrollTop = 137;
    expect(list.querySelectorAll('.ant-table-header .ant-table-filter-trigger')).toHaveLength(5);
    expect(
      Array.from(list.querySelectorAll('button')).some(
        (button) => button.textContent === '仅展示的摘要',
      ),
    ).toBe(false);
    await act(async () => list.querySelector<HTMLButtonElement>('.sm-inbox-title-link')!.click());
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(list.hidden).toBe(true);
    expect(container.querySelectorAll('.sm-content-tab')).toHaveLength(2);
    const listTab = Array.from(container.querySelectorAll<HTMLElement>('[role="tab"]')).find(
      (tab) => tab.getAttribute('aria-label') === '消息列表',
    )!;
    await act(async () => listTab.click());
    expect(list.hidden).toBe(false);
    expect(list.querySelector('.ant-table-body')).toBe(body);
    expect(body.scrollTop).toBe(137);
    await act(async () => list.querySelector<HTMLButtonElement>('.sm-inbox-title-link')!.click());
    expect(container.querySelectorAll('.sm-content-tab')).toHaveLength(2);
    await act(async () => render(1, { ...receipt, messageId: '31' }));
    expect(container.querySelectorAll('.sm-content-tab')).toHaveLength(3);
    expect(container.querySelector('[role="tabpanel"]')).toBe(list);
    const closes = container.querySelectorAll<HTMLButtonElement>('.sm-content-tab-close');
    await act(async () => closes[1]!.click());
    expect(container.querySelectorAll('.sm-content-tab')).toHaveLength(2);
    expect(list.hidden).toBe(false);
  } finally {
    await act(async () => root.unmount());
    client.clear();
    useHeaderTabsStore.setState({ activeKey: 'home' });
  }
});

it('列过滤通过普通列表控件提交服务端条件并可重置', async () => {
  useHeaderTabsStore.setState({ activeKey: 'builtin:inbox' });
  mocks.unreadSummary.mockResolvedValue({
    unreadCount: 0,
    overflow: false,
    pollingIntervalSeconds: 0,
  });
  mocks.list.mockResolvedValue({ records: [], hasMore: false });
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const container = document.createElement('div');
  document.body.append(container);
  const root = createRoot(container);
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <InboxCenter initialSection="messages" />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 20));
    });
    expect(container.querySelector('[aria-label="消息阅读状态"]')).toBeNull();
    expect(container.querySelector('[aria-label="消息时间范围"]')).toBeNull();
    const statusTrigger = container.querySelector<HTMLElement>(
      '.ant-table-header .ant-table-filter-trigger',
    )!;
    expect(container.textContent).toContain('状态：未读');
    expect(container.textContent).not.toContain('状态：是');
    await act(async () => statusTrigger.click());
    const statusPopup = document.querySelector('.sm-list-column-filter')!;
    expect(statusPopup.querySelectorAll('.ant-checkbox-wrapper')).toHaveLength(3);
    expect(statusPopup.textContent).toContain('全选');
    const selectAll = statusPopup.querySelector<HTMLInputElement>('input[type="checkbox"]')!;
    await act(async () => selectAll.click());
    const confirmStatus = Array.from(statusPopup.querySelectorAll('button')).find(
      (button) => button.textContent?.replace(/\s/g, '') === '确定',
    )!;
    await act(async () => confirmStatus.click());
    const conditions = JSON.parse(mocks.list.mock.lastCall?.[0].filters ?? '[]');
    expect(
      conditions.find((item: { field: string }) => item.field === 'readStatus').values,
    ).toEqual([false, true]);
    // 默认条件与表头共用摘要；逐个清除后请求不再隐含未读或本月限制。
    for (const close of container.querySelectorAll<HTMLElement>(
      '.sm-list-filter-summary-tag .ant-tag-close-icon',
    )) {
      await act(async () => close.click());
    }
    expect(mocks.list.mock.lastCall?.[0]).not.toHaveProperty('unreadOnly');
    expect(mocks.list.mock.lastCall?.[0]).not.toHaveProperty('monthOnly');
    expect(mocks.list.mock.lastCall?.[0].filters).toBeUndefined();
    const filter = container.querySelectorAll<HTMLElement>(
      '.ant-table-header .ant-table-filter-trigger',
    )[1]!;
    await act(async () => filter.click());
    const popup = Array.from(document.querySelectorAll('.sm-list-column-filter')).find((element) =>
      element.textContent?.includes('不为空'),
    )!;
    const notEmpty = Array.from(popup.querySelectorAll('button')).find(
      (button) => button.textContent === '不为空',
    )!;
    await act(async () => notEmpty.click());
    const apply = Array.from(popup.querySelectorAll('button')).find(
      (button) => button.textContent?.replace(/\s/g, '') === '确定',
    )!;
    await act(async () => apply.click());
    expect(mocks.list).toHaveBeenLastCalledWith(
      expect.objectContaining({
        filters: JSON.stringify([{ field: 'title', type: 'string', operator: 'NOT_EMPTY' }]),
      }),
    );
    expect(container.textContent).toContain('标题：不为空');
    await act(async () =>
      container
        .querySelector<HTMLElement>('.sm-list-filter-summary-tag .ant-tag-close-icon')!
        .click(),
    );
    expect(mocks.list).toHaveBeenLastCalledWith(expect.objectContaining({ filters: undefined }));
  } finally {
    await act(async () => root.unmount());
    client.clear();
    container.remove();
    useHeaderTabsStore.setState({ activeKey: 'home' });
  }
});
