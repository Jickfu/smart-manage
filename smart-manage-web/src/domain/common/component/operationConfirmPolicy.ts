import type { OperationConfirmType } from './operationConfirmTypes';

/** 危险操作必须使用危险按钮，且不注册 Enter 快捷确认。 */
export function getOperationConfirmPolicy(type: OperationConfirmType) {
  const dangerous = type === 'delete' || type === 'destructive';
  return { dangerous };
}
