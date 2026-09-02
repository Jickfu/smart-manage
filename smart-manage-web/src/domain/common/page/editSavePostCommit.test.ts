import { describe, expect, it, vi } from 'vitest';
import { getEditSavePostCommitFeedback, runEditSavePostCommit } from './editSavePostCommit';

describe('编辑保存后的本地同步', () => {
  it('先同步页签，再刷新缓存', async () => {
    const phases: string[] = [];
    const result = await runEditSavePostCommit({
      syncTab: () => {
        phases.push('tab');
      },
      refreshCache: async () => {
        phases.push('cache');
      },
    });
    expect(phases).toEqual(['tab', 'cache']);
    expect(result).toBe('success');
    expect(getEditSavePostCommitFeedback(result, true)).toEqual({
      type: 'success',
      message: '新增成功',
    });
  });

  it('页签失败后仍刷新缓存，并只报告页签失败', async () => {
    const refreshCache = vi.fn().mockResolvedValue(undefined);
    const result = await runEditSavePostCommit({
      syncTab: () => {
        throw new Error('registration missing');
      },
      refreshCache,
    });
    expect(refreshCache).toHaveBeenCalledOnce();
    expect(result).toBe('tab-sync-failed');
    expect(getEditSavePostCommitFeedback(result, true)).toEqual({
      type: 'error',
      message: '保存已成功，但工作台页签同步失败',
    });
  });

  it('缓存失败不否定已经完成的页签同步', async () => {
    const syncTab = vi.fn();
    const result = await runEditSavePostCommit({
      syncTab,
      refreshCache: vi.fn().mockRejectedValue(new Error('offline')),
    });
    expect(syncTab).toHaveBeenCalledOnce();
    expect(result).toBe('cache-refresh-failed');
    expect(getEditSavePostCommitFeedback(result, false)).toEqual({
      type: 'warning',
      message: '保存已成功，但页面数据刷新失败',
    });
  });

  it('两个阶段均失败时保留完整结果', async () => {
    const result = await runEditSavePostCommit({
      syncTab: vi.fn().mockRejectedValue(new Error('tab')),
      refreshCache: vi.fn().mockRejectedValue(new Error('cache')),
    });
    expect(result).toBe('tab-sync-and-cache-refresh-failed');
    expect(getEditSavePostCommitFeedback(result, false)).toEqual({
      type: 'error',
      message: '保存已成功，但页签同步和页面数据刷新均失败',
    });
    expect(getEditSavePostCommitFeedback('success', false).message).toBe('保存成功');
  });
});
