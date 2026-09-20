// @vitest-environment jsdom
import { act } from 'react';
import type { ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OperationConfirmProvider } from '@/domain/common/component/OperationConfirmProvider';
import { OperationType } from '@/domain/common/page/types';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { AppVO } from '@/domain/sys/base/app/types';
import { useWorkbenchStore } from '@/stores/workbench';
import ContentTabErrorBoundary from './ContentTabErrorBoundary';

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
const normalTabKey = createBillTabKey(componentKey, 'normal');
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

function ThrowingPage(): never {
  throw new Error('测试页签渲染失败');
}

function findButton(container: HTMLElement, text: string) {
  const button = [...container.querySelectorAll('button')].find(
    (item) => item.textContent?.replace(/\s/g, '') === text,
  );
  if (!button) throw new Error(`未找到按钮：${text}`);
  return button as HTMLButtonElement;
}

describe('ContentTabErrorBoundary', () => {
  beforeEach(() => {
    vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
    useWorkbenchStore.setState({
      workspaces: {},
      beforeCloseCallbacks: {},
      failedCloseCallbacks: {},
      capacityNotice: undefined,
    });
    componentRegistry[componentKey] = {
      featureKey: 'sys/base/user',
      title: '用户编辑',
      pageType: 'EDIT',
      component: () => null,
    };
    const store = useWorkbenchStore.getState();
    store.initWorkspace(appNumber, appInfo);
    store.openBillTab(appNumber, componentKey, 'failed', OperationType.EDIT);
    store.openBillTab(appNumber, componentKey, 'normal', OperationType.EDIT);
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('真实子页面渲染失败时注册故障守卫，并在边界卸载时清理', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const container = document.createElement('div');
    document.body.append(container);
    const root = createRoot(container);

    try {
      await act(async () => {
        root.render(
          <OperationConfirmProvider>
            <ContentTabErrorBoundary appNumber={appNumber} tabKey={tabKey} closable>
              <ThrowingPage />
            </ContentTabErrorBoundary>
            <input aria-label="正常页签输入" defaultValue="保留的输入" />
          </OperationConfirmProvider>,
        );
      });

      expect(container.textContent).toContain('当前页面发生异常');
      expect(container.querySelector<HTMLInputElement>('[aria-label="正常页签输入"]')?.value).toBe(
        '保留的输入',
      );
      expect(
        useWorkbenchStore.getState().failedCloseCallbacks[`${appNumber}:${tabKey}`],
      ).toBeTypeOf('function');
      expect(consoleError).toHaveBeenCalled();

      await act(async () => findButton(container, '关闭当前页签').click());
      expect(container.textContent).toContain('关闭故障页面');
      await act(async () => findButton(container, '取消').click());
      expect(useWorkbenchStore.getState().workspaces[appNumber]?.contentTabs).toHaveLength(3);
      expect(container.querySelector<HTMLInputElement>('[aria-label="正常页签输入"]')?.value).toBe(
        '保留的输入',
      );

      await act(async () => findButton(container, '关闭当前页签').click());
      await act(async () => findButton(container, '继续关闭').click());
      await act(async () => Promise.resolve());
      expect(useWorkbenchStore.getState().workspaces[appNumber]?.contentTabs).toHaveLength(2);
      expect(
        useWorkbenchStore.getState().failedCloseCallbacks[`${appNumber}:${tabKey}`],
      ).toBeUndefined();
    } finally {
      await act(async () => root.unmount());
      container.remove();
    }

    expect(
      useWorkbenchStore.getState().failedCloseCallbacks[`${appNumber}:${tabKey}`],
    ).toBeUndefined();
  });

  it('刷新先执行全部正常守卫，守卫拒绝或最终取消都不会刷新', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const normalGuard = vi.fn().mockResolvedValue(false);
    useWorkbenchStore.getState().registerBeforeClose(appNumber, normalTabKey, normalGuard);
    const initialLocation = window.location.href;
    const container = document.createElement('div');
    document.body.append(container);
    const root = createRoot(container);

    try {
      await act(async () => {
        root.render(
          <OperationConfirmProvider>
            <ContentTabErrorBoundary appNumber={appNumber} tabKey={tabKey} closable>
              <ThrowingPage />
            </ContentTabErrorBoundary>
          </OperationConfirmProvider>,
        );
      });

      await act(async () => findButton(container, '刷新整个系统页面').click());
      expect(normalGuard).toHaveBeenCalledOnce();
      expect(container.querySelector('[role="dialog"]')).toBeNull();
      expect(window.location.href).toBe(initialLocation);

      normalGuard.mockResolvedValue(true);
      await act(async () => findButton(container, '刷新整个系统页面').click());
      expect(normalGuard).toHaveBeenCalledTimes(2);
      expect(container.textContent).toContain('刷新整个系统页面');
      expect(container.textContent).not.toContain('关闭故障页面');

      await act(async () => findButton(container, '取消').click());
      expect(window.location.href).toBe(initialLocation);
      expect(useWorkbenchStore.getState().workspaces[appNumber]?.contentTabs).toHaveLength(3);
    } finally {
      await act(async () => root.unmount());
      container.remove();
    }
  });
});
