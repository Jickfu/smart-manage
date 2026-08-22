import { useContext } from 'react';
import { OperationConfirmContext } from './OperationConfirmProvider';

/** 调用应用级操作确认弹框。 */
export function useOperationConfirm() {
  const confirmOperation = useContext(OperationConfirmContext);
  if (!confirmOperation) {
    throw new Error('useOperationConfirm 必须在 OperationConfirmProvider 内使用');
  }
  return confirmOperation;
}
