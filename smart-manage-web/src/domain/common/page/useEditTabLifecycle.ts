import { useMemo } from 'react';
import { useWorkbenchStore } from '@/stores/workbench';
import { createBillTabKey } from './tabKeys';
import { OperationType } from './types';
import type { PageComponentProps } from './types';

type EditTabLifecycleProps = Pick<
  PageComponentProps,
  'appNumber' | 'tabKey' | 'componentKey' | 'operationType'
>;

/** 独立于 React 渲染的页签动作，所有状态变更仍通过工作台正式操作完成。 */
export function createEditTabLifecycle({
  appNumber,
  tabKey,
  componentKey,
  operationType,
}: EditTabLifecycleProps) {
  const isAddNew = operationType === OperationType.ADDNEW;
  return {
    isAddNew,
    promoteToPersistedTab(savedId: string) {
      if (!isAddNew) return;
      const nextKey = createBillTabKey(componentKey, savedId);
      const workbench = useWorkbenchStore.getState();
      workbench.replaceContentTab(appNumber, tabKey, {
        key: nextKey,
        closable: true,
        componentKey,
        pageType: 'EDIT',
        operationType: OperationType.EDIT,
        billId: savedId,
      });
      // 保存期间用户可能切换过页签，保留原有保存后激活真实单据的行为。
      workbench.activateContentTab(appNumber, nextKey);
    },
    exit: () => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey),
  };
}

/** 只负责新增识别、临时页签晋升和退出，不持有领域保存或缓存语义。 */
export function useEditTabLifecycle({
  appNumber,
  tabKey,
  componentKey,
  operationType,
}: EditTabLifecycleProps) {
  return useMemo(
    () => createEditTabLifecycle({ appNumber, tabKey, componentKey, operationType }),
    [appNumber, tabKey, componentKey, operationType],
  );
}
