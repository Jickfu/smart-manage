import type { ReactNode } from 'react';

type OperationConfirmType = 'delete' | 'destructive' | 'warning' | 'normal';

interface OperationConfirmOptions {
  type: OperationConfirmType;
  title?: ReactNode;
  description: ReactNode;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => unknown | Promise<unknown>;
}

type ConfirmOperation = (options: OperationConfirmOptions) => Promise<boolean>;

export type { ConfirmOperation, OperationConfirmOptions, OperationConfirmType };
