// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { OperationType } from '@/domain/common/page/types';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { AppVO } from '@/domain/sys/base/app/types';
import {
  RETAINED_PAGE_LIMIT,
  retainedPageLimitMessage,
  useWorkbenchStore,
} from '@/stores/workbench';
import Workbench from './Workbench';

const mocks = vi.hoisted(() => ({ warning: vi.fn() }));
vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => ({
    warning: mocks.warning,
    info: vi.fn(),
    fromError: vi.fn(),
  }),
}));
vi.mock('@tanstack/react-query', () => ({
  useQuery: () => ({
    data: { routes: [] },
    error: null,
    isError: false,
    isLoading: false,
    isSuccess: true,
    refetch: vi.fn(),
  }),
}));
vi.mock('./AppSidebar', () => ({ default: () => null }));
vi.mock('./ContentTabsBar', () => ({ default: () => null }));
vi.mock('./PageRenderer', () => ({ default: () => null }));
vi.mock('./ApplicationHome', () => ({ default: () => null }));
vi.mock('./ExternalLinkFrame', () => ({ default: () => null }));
vi.mock('./ContentTabErrorBoundary', () => ({
  default: ({ children }: { children: ReactNode }) => children,
}));

const appNumber = 'base';
const componentKey = 'sys/base/user/edit';
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

describe('Workbench 容量反馈', () => {
  beforeEach(() => {
    vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
    mocks.warning.mockReset();
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
    for (let index = 0; index < RETAINED_PAGE_LIMIT - 1; index += 1) {
      store.openBillTab(appNumber, componentKey, String(index), OperationType.VIEW);
    }
    const notice = useWorkbenchStore.getState().capacityNotice;
    if (notice) store.consumeCapacityNotice(notice.revision);
  });

  afterEach(() => vi.unstubAllGlobals());

  it('普通业务页入口满额时提示一次并消费事件，重渲染不重播', async () => {
    const container = document.createElement('div');
    const root = createRoot(container);
    try {
      await act(async () => {
        root.render(
          <Workbench appNumber={appNumber} appActive onInitialEntryConsumed={() => {}} />,
        );
      });
      await act(async () => {
        useWorkbenchStore
          .getState()
          .openBillTab(appNumber, componentKey, 'rejected', OperationType.EDIT);
      });

      expect(mocks.warning).toHaveBeenCalledTimes(1);
      expect(mocks.warning).toHaveBeenCalledWith(
        retainedPageLimitMessage(),
        expect.objectContaining({ key: expect.stringContaining('workbench-capacity-') }),
      );
      expect(useWorkbenchStore.getState().capacityNotice).toBeUndefined();

      await act(async () => {
        root.render(
          <Workbench appNumber={appNumber} appActive onInitialEntryConsumed={() => {}} />,
        );
      });
      expect(mocks.warning).toHaveBeenCalledTimes(1);
    } finally {
      await act(async () => root.unmount());
    }
  });
});
