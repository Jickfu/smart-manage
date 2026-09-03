export type EditSavePostCommitResult =
  | 'success'
  | 'tab-sync-failed'
  | 'cache-refresh-failed'
  | 'tab-sync-and-cache-refresh-failed';

interface EditSavePostCommitActions {
  syncTab: () => void | Promise<unknown>;
  refreshCache: () => Promise<unknown>;
}

/** 远端保存已经成功；两个本地同步阶段独立执行，不能重新归类为保存失败。 */
export async function runEditSavePostCommit({
  syncTab,
  refreshCache,
}: EditSavePostCommitActions): Promise<EditSavePostCommitResult> {
  let tabSyncFailed = false;
  let cacheRefreshFailed = false;
  try {
    await syncTab();
  } catch {
    tabSyncFailed = true;
  }
  try {
    await refreshCache();
  } catch {
    cacheRefreshFailed = true;
  }
  if (tabSyncFailed && cacheRefreshFailed) return 'tab-sync-and-cache-refresh-failed';
  if (tabSyncFailed) return 'tab-sync-failed';
  if (cacheRefreshFailed) return 'cache-refresh-failed';
  return 'success';
}

export function getEditSavePostCommitFeedback(
  result: EditSavePostCommitResult,
  isAddNew: boolean,
): { type: 'success' | 'warning' | 'error'; message: string } {
  if (result === 'success') {
    return { type: 'success', message: isAddNew ? '新增成功' : '保存成功' };
  }
  const warnings = {
    'tab-sync-failed': '保存已成功，但工作台页签同步失败',
    'cache-refresh-failed': '保存已成功，但页面数据刷新失败',
    'tab-sync-and-cache-refresh-failed': '保存已成功，但页签同步和页面数据刷新均失败',
  };
  return {
    type: result === 'cache-refresh-failed' ? 'warning' : 'error',
    message: warnings[result],
  };
}
