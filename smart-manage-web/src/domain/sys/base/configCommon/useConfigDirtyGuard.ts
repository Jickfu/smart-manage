import { useBeforeCloseGuard } from '@/domain/common/page/useBeforeCloseGuard';

export function useConfigDirtyGuard(appNumber: string, tabKey: string, dirty: boolean): void {
  useBeforeCloseGuard(appNumber, tabKey, dirty);
}
