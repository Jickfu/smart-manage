import { useEffect } from 'react';
import { App } from 'antd';
import { useWorkbenchStore } from '@/stores/workbench';

export function useConfigDirtyGuard(appNumber: string, tabKey: string, dirty: boolean): void {
  const { modal } = App.useApp();
  useEffect(() => {
    const store = useWorkbenchStore.getState();
    store.registerBeforeClose(appNumber, tabKey, async () => {
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
  }, [appNumber, dirty, modal, tabKey]);
}
