import { createContext, useCallback, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import OperationConfirmModal from './OperationConfirmModal';
import type { ConfirmOperation, OperationConfirmOptions } from './operationConfirmTypes';

interface PendingConfirmation {
  options: OperationConfirmOptions;
  resolve: (confirmed: boolean) => void;
}

const OperationConfirmContext = createContext<ConfirmOperation | null>(null);

/** 在应用级集中管理操作确认，确保同一时刻只有一个确认流程。 */
export function OperationConfirmProvider({ children }: { children: ReactNode }) {
  const pendingRef = useRef<PendingConfirmation | null>(null);
  const [options, setOptions] = useState<OperationConfirmOptions | null>(null);
  const [confirming, setConfirming] = useState(false);

  const finish = useCallback((confirmed: boolean) => {
    const pending = pendingRef.current;
    pendingRef.current = null;
    setOptions(null);
    setConfirming(false);
    pending?.resolve(confirmed);
  }, []);

  const confirmOperation = useCallback<ConfirmOperation>((nextOptions) => {
    if (pendingRef.current) {
      // 防止快速重复点击创建叠加弹框；既有确认流程保持不变。
      return Promise.resolve(false);
    }
    return new Promise<boolean>((resolve) => {
      pendingRef.current = { options: nextOptions, resolve };
      setOptions(nextOptions);
    });
  }, []);

  const handleConfirm = useCallback(async () => {
    const pending = pendingRef.current;
    if (!pending || confirming) return;
    setConfirming(true);
    try {
      await pending.options.onConfirm?.();
      finish(true);
    } catch {
      // Mutation 统一负责展示业务错误；保留弹框供用户重试或取消。
      setConfirming(false);
    }
  }, [confirming, finish]);

  const contextValue = useMemo(() => confirmOperation, [confirmOperation]);
  return (
    <OperationConfirmContext.Provider value={contextValue}>
      {children}
      <OperationConfirmModal
        open={options != null}
        options={options}
        confirming={confirming}
        onCancel={() => {
          if (!confirming) finish(false);
        }}
        onConfirm={() => void handleConfirm()}
      />
    </OperationConfirmContext.Provider>
  );
}

export { OperationConfirmContext };
