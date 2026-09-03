import { useBeforeCloseGuard } from '@/domain/common/page/tab/useBeforeCloseGuard';

export function useConfigDirtyGuard(appNumber: string, tabKey: string, dirty: boolean): void {
  useBeforeCloseGuard(appNumber, tabKey, dirty);
}
