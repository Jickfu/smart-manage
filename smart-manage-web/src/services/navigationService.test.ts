import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AppVO } from '@/domain/sys/base/app/types';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import { OperationType } from '@/domain/common/page/types';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { RETAINED_PAGE_LIMIT, useWorkbenchStore } from '@/stores/workbench';
import type { MenuVO } from '@/types/api';
import { openApp, openMenuItem } from './navigationService';

const { openByNumber } = vi.hoisted(() => ({
  openByNumber: vi.fn<(appNumber: string) => Promise<AppVO>>(),
}));

vi.mock('@/domain/sys/base/app/api', () => ({
  openByNumber: (appNumber: string) => openByNumber(appNumber),
}));

const componentKey = 'sys/base/user';
const fillerComponentKey = 'sys/base/user/edit';
const appInfo = (number: string): AppVO => ({
  id: number,
  domainNumber: 'sys',
  number,
  name: `应用 ${number}`,
  icon: 'app',
  iconColor: '#165dff',
  seq: 1,
  description: '',
});
const menuItem: MenuVO = {
  id: 'menu-user',
  number: 'user',
  name: '用户管理',
  icon: '',
  level: 1,
  routes: [],
  component: componentKey,
  targetType: 'INTERNAL_PAGE',
};

describe('navigationService', () => {
  beforeEach(() => {
    openByNumber.mockReset();
    componentRegistry[componentKey] = {
      featureKey: componentKey,
      title: '用户管理',
      pageType: 'LIST',
      component: () => null,
    };
    componentRegistry[fillerComponentKey] = {
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
    useHeaderTabsStore.setState({
      tabs: [
        { key: 'home', label: '首页', type: 'system', pinned: false, loaded: true },
        { key: 'apps', label: '应用', type: 'system', pinned: false, loaded: true },
      ],
      activeKey: 'home',
      activeHistory: ['home'],
    });
  });

  it('49 个页面时拒绝需要同时创建应用首页和菜单的组合导航且状态不变', async () => {
    const store = useWorkbenchStore.getState();
    store.initWorkspace('A', appInfo('A'));
    useHeaderTabsStore.getState().addAppTab('A', '应用 A');
    for (let index = 0; index < RETAINED_PAGE_LIMIT - 2; index += 1) {
      store.openBillTab('A', fillerComponentKey, String(index), OperationType.VIEW);
    }
    const workspacesBefore = useWorkbenchStore.getState().workspaces;
    const headerBefore = useHeaderTabsStore.getState();
    openByNumber.mockResolvedValue(appInfo('B'));

    expect(await openMenuItem('B', menuItem)).toEqual({ status: 'capacity-exceeded' });
    expect(useWorkbenchStore.getState().workspaces).toBe(workspacesBefore);
    expect(useHeaderTabsStore.getState().tabs).toEqual(headerBefore.tabs);
    expect(useHeaderTabsStore.getState().activeKey).toBe('A');
  });

  it('50 个页面时拒绝已加载非当前应用的新菜单且不切换应用', async () => {
    const store = useWorkbenchStore.getState();
    store.initWorkspace('A', appInfo('A'));
    store.initWorkspace('B', appInfo('B'));
    useHeaderTabsStore.getState().addAppTab('B', '应用 B');
    useHeaderTabsStore.getState().addAppTab('A', '应用 A');
    for (let index = 0; index < RETAINED_PAGE_LIMIT - 2; index += 1) {
      store.openBillTab('A', fillerComponentKey, String(index), OperationType.VIEW);
    }

    expect(await openMenuItem('B', menuItem)).toEqual({ status: 'capacity-exceeded' });
    expect(useHeaderTabsStore.getState().activeKey).toBe('A');
    expect(useWorkbenchStore.getState().workspaces.B?.contentTabs).toHaveLength(1);
  });

  it('异步组合导航被新导航取代后不再创建工作区或页签', async () => {
    let resolveApp: ((value: AppVO) => void) | undefined;
    openByNumber.mockReturnValue(
      new Promise<AppVO>((resolve) => {
        resolveApp = resolve;
      }),
    );

    const oldNavigation = openMenuItem('A', menuItem);
    await openApp('home');
    resolveApp?.(appInfo('A'));

    expect(await oldNavigation).toEqual({ status: 'superseded' });
    expect(useWorkbenchStore.getState().workspaces.A).toBeUndefined();
    expect(useHeaderTabsStore.getState().activeKey).toBe('home');
  });
});
