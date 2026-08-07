import { useEffect } from 'react';
import { App } from 'antd';
import { useWorkbenchStore } from '@/stores/workbench';

type DirtyState = boolean | { readonly current: boolean };

/** 注册统一的页签关闭确认；支持普通状态和无需触发重渲染的 ref 状态。 */
export function useBeforeCloseGuard(
  appNumber: string | undefined,
  tabKey: string | undefined,
  dirtyState: DirtyState,
): void {
  const { modal } = App.useApp();
  useEffect(() => {
    if (!appNumber || !tabKey) return;
    const store = useWorkbenchStore.getState();
    store.registerBeforeClose(appNumber, tabKey, async () => {
      const dirty = typeof dirtyState === 'boolean' ? dirtyState : dirtyState.current;
      if (!dirty) return true;
      return new Promise<boolean>((resolve) => {
        modal.confirm({
          title: '存在未保存的修改',
          content: '关闭页面将丢失当前修改，是否继续？',
          okText: '继续关闭',
          cancelText: '留在页面',
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        });
      });
    });
    return () => store.unregisterBeforeClose(appNumber, tabKey);
  }, [appNumber, dirtyState, modal, tabKey]);
}
