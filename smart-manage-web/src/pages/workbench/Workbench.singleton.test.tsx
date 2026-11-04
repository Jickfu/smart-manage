// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { expect, it, vi } from 'vitest';
import type { MenuVO } from '@/types/api';
import { OperationType } from '@/domain/common/page/types';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import registrations from '@/domain/sys/base/passwordConfig/pageRegistration';
import { useWorkbenchStore } from '@/stores/workbench';
import Workbench from './Workbench';

const feedback = vi.hoisted(() => ({ warning: vi.fn(), info: vi.fn(), fromError: vi.fn() }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => feedback,
}));
vi.mock('@tanstack/react-query', () => ({
  useQuery: () => ({ data: { routes: [] }, isSuccess: true }),
}));
vi.mock('./AppSidebar', () => ({
  default: ({ onItemClick }: { onItemClick: (item: MenuVO) => void }) => (
    <button
      onClick={() =>
        onItemClick({
          id: '560000000000000210',
          name: '密码策略',
          icon: '',
          level: 1,
          routes: [],
          targetType: 'INTERNAL_PAGE',
          component: 'sys/base/password-config',
        })
      }
    >
      密码策略
    </button>
  ),
}));
vi.mock('./ContentTabsBar', () => ({ default: () => null }));
vi.mock('./PageRenderer', () => ({ default: () => null }));
vi.mock('./ApplicationHome', () => ({ default: () => null }));
vi.mock('./ExternalLinkFrame', () => ({ default: () => null }));
vi.mock('./ContentTabErrorBoundary', () => ({
  default: ({ children }: { children: ReactNode }) => children,
}));

it('密码策略菜单按真实 EDIT 注册打开，重复点击复用页签并保留关闭守卫', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const registration = registrations[0]!;
  const previousRegistration = componentRegistry[registration.componentKey];
  componentRegistry[registration.componentKey] = registration;
  useWorkbenchStore.setState({
    workspaces: {},
    beforeCloseCallbacks: {},
    failedCloseCallbacks: {},
    capacityNotice: undefined,
  });
  useWorkbenchStore.getState().initWorkspace('base', {
    id: '31',
    domainNumber: 'sys',
    number: 'base',
    name: '系统管理',
    icon: '',
    iconColor: '',
    seq: 1,
    description: '',
  });
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () => {
      root.render(<Workbench appNumber="base" appActive onInitialEntryConsumed={() => {}} />);
    });
    await act(async () => container.querySelector('button')!.click());
    const workspace = useWorkbenchStore.getState().workspaces.base!;
    const tab = workspace.contentTabs.find(
      (item) => item.componentKey === registration.componentKey,
    )!;
    expect(tab).toMatchObject({
      label: '密码策略',
      pageType: 'EDIT',
      operationType: OperationType.EDIT,
    });
    expect(tab.billId).toBeUndefined();
    expect(tab.temporary).not.toBe(true);
    const guard = vi.fn().mockResolvedValue(false);
    useWorkbenchStore.getState().registerBeforeClose('base', tab.key, guard);
    await act(async () => container.querySelector('button')!.click());
    expect(useWorkbenchStore.getState().workspaces.base!.contentTabs).toHaveLength(
      workspace.contentTabs.length,
    );
    await act(async () => useWorkbenchStore.getState().removeContentTab('base', tab.key));
    expect(guard).toHaveBeenCalledOnce();
    expect(useWorkbenchStore.getState().workspaces.base!.activeContentTabKey).toBe(tab.key);
    expect(feedback.fromError).not.toHaveBeenCalled();
  } finally {
    await act(async () => root.unmount());
    if (previousRegistration) componentRegistry[registration.componentKey] = previousRegistration;
    else delete componentRegistry[registration.componentKey];
    vi.unstubAllGlobals();
  }
});
