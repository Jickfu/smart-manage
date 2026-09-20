// @vitest-environment jsdom
import { act, useEffect } from 'react';
import type { ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OperationConfirmProvider } from '@/domain/common/component/OperationConfirmProvider';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { OperationType } from '@/domain/common/page/types';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { AppVO } from '@/domain/sys/base/app/types';
import { useWorkbenchStore } from '@/stores/workbench';
import ContentTabsBar from './ContentTabsBar';

vi.mock('@/domain/common/component/AppModal', () => ({
  default: ({
    title,
    children,
    footer,
  }: {
    title: ReactNode;
    children: ReactNode;
    footer: ReactNode;
  }) => (
    <div role="dialog">
      <div>{title}</div>
      {children}
      {footer}
    </div>
  ),
}));

const appNumber = 'base';
const componentKey = 'sys/base/user/edit';
const tabKey = createBillTabKey(componentKey, 'failed');
const appInfo: AppVO = {
  id: '1',
  domainNumber: 'sys',
  number: appNumber,
  name: '系统管理',
  icon: 'app',
  iconColor: '#165dff',
  seq: 1,
  description: '',
};

function FailedGuardRegistration() {
  const confirmOperation = useOperationConfirm();
  useEffect(() => {
    useWorkbenchStore.getState().registerFailedClose(appNumber, tabKey, () =>
      confirmOperation({
        type: 'warning',
        title: '关闭故障页面',
        description: '故障页内容可能无法恢复，是否继续？',
        confirmText: '继续关闭',
        cancelText: '取消',
      }),
    );
    return () => useWorkbenchStore.getState().unregisterFailedClose(appNumber, tabKey);
  }, [confirmOperation]);
  return null;
}

function findButton(container: HTMLElement, text: string) {
  const button = [...container.querySelectorAll('button')].find(
    (item) =>
      item.textContent?.replace(/\s/g, '') === text || item.getAttribute('aria-label') === text,
  );
  if (!button) throw new Error(`未找到按钮：${text}`);
  return button as HTMLButtonElement;
}

describe('ContentTabsBar', () => {
  beforeEach(() => {
    vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
    vi.stubGlobal(
      'ResizeObserver',
      class {
        observe() {}
        disconnect() {}
      },
    );
    Element.prototype.scrollIntoView = vi.fn();
    componentRegistry[componentKey] = {
      featureKey: 'sys/base/user',
      title: '用户编辑',
      pageType: 'EDIT',
      component: () => null,
    };
    useWorkbenchStore.setState({
      workspaces: {},
      beforeCloseCallbacks: {},
      failedCloseCallbacks: {},
      capacityNotice: undefined,
    });
    const store = useWorkbenchStore.getState();
    store.initWorkspace(appNumber, appInfo);
    store.openBillTab(appNumber, componentKey, 'failed', OperationType.EDIT);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('关闭全部先结束批量确认，再允许真实 Provider 展示故障页确认', async () => {
    const container = document.createElement('div');
    document.body.append(container);
    const root = createRoot(container);
    try {
      await act(async () => {
        root.render(
          <OperationConfirmProvider>
            <FailedGuardRegistration />
            <ContentTabsBar appNumber={appNumber} />
          </OperationConfirmProvider>,
        );
      });

      await act(async () => findButton(container, '关闭全部页签').click());
      expect(container.textContent).toContain('确定关闭全部 1 个页签吗？');

      await act(async () => findButton(container, '确定').click());
      await act(async () => Promise.resolve());
      expect(container.textContent).toContain('关闭故障页面');
      expect(useWorkbenchStore.getState().workspaces[appNumber]?.contentTabs).toHaveLength(2);

      await act(async () => findButton(container, '继续关闭').click());
      await act(async () => Promise.resolve());
      expect(useWorkbenchStore.getState().workspaces[appNumber]?.contentTabs).toHaveLength(1);
    } finally {
      await act(async () => root.unmount());
      container.remove();
    }
  });
});
