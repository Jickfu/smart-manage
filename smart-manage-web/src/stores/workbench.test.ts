import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OperationType } from '@/domain/common/page/types';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import type { AppVO } from '@/domain/sys/app/types';
import { useWorkbenchStore } from './workbench';

const APP_NUMBER = 'scm';
const COMPONENT_KEY = 'scm/procurement/purchase-requisition/edit';

const appInfo: AppVO = {
  id: '1',
  cloudNumber: 'scm',
  number: APP_NUMBER,
  name: '采购管理',
  icon: 'app',
  iconColor: '#165dff',
  seq: 1,
  description: '',
};

describe('workbench store', () => {
  beforeEach(() => {
    useWorkbenchStore.setState({ workspaces: {}, beforeCloseCallbacks: {} });
    useWorkbenchStore.getState().initWorkspace(APP_NUMBER, appInfo);
  });

  it('保存新增单据后将临时页签替换为真实单据页签', () => {
    const store = useWorkbenchStore.getState();
    expect(store.openAddNewTab(APP_NUMBER, COMPONENT_KEY, '新增采购申请')).toBe('opened');
    const temporaryTab = useWorkbenchStore
      .getState()
      .workspaces[APP_NUMBER]!.contentTabs.find((tab) => tab.temporary);
    expect(temporaryTab).toBeDefined();

    const billId = '1987654321098765432';
    const realTabKey = createBillTabKey(COMPONENT_KEY, billId);
    store.replaceContentTab(APP_NUMBER, temporaryTab!.key, {
      key: realTabKey,
      label: '采购申请',
      closable: true,
      componentKey: COMPONENT_KEY,
      pageType: 'EDIT',
      operationType: OperationType.EDIT,
      billId,
    });

    const workspace = useWorkbenchStore.getState().workspaces[APP_NUMBER]!;
    expect(workspace.activeContentTabKey).toBe(realTabKey);
    expect(workspace.contentTabs.some((tab) => tab.key === temporaryTab!.key)).toBe(false);
    expect(workspace.contentTabs.find((tab) => tab.key === realTabKey)?.billId).toBe(billId);
  });

  it('关闭存在脏数据的页签时尊重关闭保护结果', async () => {
    const store = useWorkbenchStore.getState();
    store.openBillTab(APP_NUMBER, COMPONENT_KEY, '采购申请', '100', OperationType.EDIT);
    const tabKey = createBillTabKey(COMPONENT_KEY, '100');
    const guard = vi.fn().mockResolvedValue(false);
    store.registerBeforeClose(APP_NUMBER, tabKey, guard);

    await store.removeContentTab(APP_NUMBER, tabKey);

    expect(guard).toHaveBeenCalledOnce();
    expect(
      useWorkbenchStore
        .getState()
        .workspaces[APP_NUMBER]!.contentTabs.some((tab) => tab.key === tabKey),
    ).toBe(true);
  });

  it('达到内容页签上限后拒绝继续打开页签', () => {
    const store = useWorkbenchStore.getState();
    for (let index = 0; index < 20; index += 1) {
      expect(
        store.openBillTab(
          APP_NUMBER,
          COMPONENT_KEY,
          `采购申请${index}`,
          String(index),
          OperationType.VIEW,
        ),
      ).toBe('opened');
    }

    expect(
      store.openBillTab(APP_NUMBER, COMPONENT_KEY, '超限页签', 'overflow', OperationType.VIEW),
    ).toBe('limit_reached');
  });
});
