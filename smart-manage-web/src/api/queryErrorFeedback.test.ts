import { afterEach, describe, expect, it, vi } from 'vitest';
import { CancelledError, QueryObserver } from '@tanstack/react-query';
import { CanceledError } from 'axios';
import { ApiError } from './ApiError';
import {
  createQueryFeedbackRuntime,
  getBlockingQueryError,
  type QueryErrorMeta,
} from './queryErrorFeedback';
import {
  getEditSavePostCommitFeedback,
  runEditSavePostCommit,
} from '@/domain/common/page/edit/editSavePostCommit';

const networkError = () => new ApiError({ source: 'NETWORK', message: 'socket internals' });
const apiError = (apiCode: number, traceId = '') =>
  new ApiError({ source: 'API', message: '请求被拒绝', httpStatus: 200, apiCode, traceId });
const cleanups: Array<() => void> = [];
afterEach(async () => {
  for (const cleanup of cleanups.splice(0).reverse()) cleanup();
  await Promise.resolve();
});

function setup() {
  const runtime = createQueryFeedbackRuntime();
  const sink = { show: vi.fn(), close: vi.fn() };
  cleanups.push(runtime.connect(sink));
  runtime.queryClient.setDefaultOptions({ queries: { retry: false, gcTime: Infinity } });
  return { ...runtime, sink };
}

describe('blocking query state', () => {
  it('distinguishes unavailable data, valid empty data and placeholders', () => {
    const error = networkError();
    expect(getBlockingQueryError({ data: undefined, error })).toBe(error);
    expect(getBlockingQueryError({ data: [], error })).toBeNull();
    expect(getBlockingQueryError({ data: null, error })).toBeNull();
    expect(getBlockingQueryError({ data: [], error, isPlaceholderData: true })).toBe(error);
  });

  it.each([403, 404, 100403, 100404])('blocks stale resources for %s', (code) => {
    const error =
      code < 1000
        ? new ApiError({ source: 'HTTP', message: '', httpStatus: code })
        : apiError(code);
    expect(getBlockingQueryError({ data: { id: 'old' }, error })).toBe(error);
  });

  it.each([new CancelledError(), new CanceledError(), apiError(100401), apiError(100419)])(
    'leaves special owners alone',
    (error) => {
      expect(getBlockingQueryError({ data: undefined, error })).toBeNull();
    },
  );
});

describe('query fault ownership and lifetime', () => {
  it('waits for the final retry and defaults to system feedback', async () => {
    const { queryClient, sink } = setup();
    const queryFn = vi.fn().mockRejectedValue(networkError());
    await expect(
      queryClient.fetchQuery({ queryKey: ['retry'], queryFn, retry: 1, retryDelay: 0 }),
    ).rejects.toThrow();
    expect(queryFn).toHaveBeenCalledTimes(2);
    expect(sink.show).toHaveBeenCalledTimes(1);
  });

  it.each(['local', 'local-initial'] as QueryErrorMeta['errorPresentation'][])(
    'honors zero-observer metadata %s',
    async (errorPresentation) => {
      const { queryClient, sink } = setup();
      await queryClient
        .fetchQuery({
          queryKey: ['local'],
          queryFn: () => Promise.reject(networkError()),
          meta: { errorPresentation },
        })
        .catch(() => undefined);
      expect(sink.show).not.toHaveBeenCalled();
    },
  );

  it('uses each enabled observer rather than the last query metadata', async () => {
    const { queryClient, sink } = setup();
    const queryKey = ['shared'];
    const queryFn = () => Promise.reject(networkError());
    const local = new QueryObserver(queryClient, {
      queryKey,
      queryFn,
      meta: { errorPresentation: 'local-initial' },
    });
    const global = new QueryObserver(queryClient, { queryKey, queryFn });
    cleanups.push(
      local.subscribe(() => undefined),
      global.subscribe(() => undefined),
    );
    await global.refetch();
    expect(sink.show).toHaveBeenCalledTimes(1);
    queryClient.setQueryData(queryKey, []);
    await global.refetch();
    expect(sink.show).toHaveBeenCalledTimes(1);
  });

  it('does not let a disabled local observer suppress an enabled global observer', async () => {
    const { queryClient, sink } = setup();
    const queryKey = ['disabled'];
    const queryFn = () => Promise.reject(networkError());
    const local = new QueryObserver(queryClient, {
      queryKey,
      queryFn,
      enabled: false,
      meta: { errorPresentation: 'local' },
    });
    const global = new QueryObserver(queryClient, { queryKey, queryFn });
    cleanups.push(
      local.subscribe(() => undefined),
      global.subscribe(() => undefined),
    );
    await global.refetch();
    expect(sink.show).toHaveBeenCalledTimes(1);
  });

  it('keeps resource denials local even with cached data', async () => {
    const { queryClient, sink } = setup();
    queryClient.setQueryData(['resource'], { id: 'old' });
    await queryClient
      .fetchQuery({
        queryKey: ['resource'],
        queryFn: () => Promise.reject(apiError(100404)),
        meta: { errorPresentation: 'local-initial' },
      })
      .catch(() => undefined);
    expect(sink.show).not.toHaveBeenCalled();
  });

  it.each([new CanceledError(), apiError(100401), apiError(100419)])(
    'does not duplicate special feedback',
    async (error) => {
      const { queryClient, sink } = setup();
      await queryClient
        .fetchQuery({ queryKey: ['special'], queryFn: () => Promise.reject(error) })
        .catch(() => undefined);
      expect(sink.show).not.toHaveBeenCalled();
    },
  );

  it('deduplicates across query keys and trace IDs until every member recovers', async () => {
    const { queryClient, sink } = setup();
    const fail = (key: string) =>
      queryClient
        .fetchQuery({ queryKey: [key], queryFn: () => Promise.reject(apiError(100500, key)) })
        .catch(() => undefined);
    await fail('first');
    await fail('second');
    expect(sink.show).toHaveBeenCalledTimes(1);
    // 关闭或 maxCount 淘汰仅改变 UI，不会告诉 runtime 故障已经恢复。
    sink.close(sink.show.mock.calls[0]![1]);
    sink.close.mockClear();
    await fail('first');
    expect(sink.show).toHaveBeenCalledTimes(1);
    await queryClient.fetchQuery({ queryKey: ['first'], queryFn: async () => [] });
    expect(sink.close).not.toHaveBeenCalled();
    await queryClient.fetchQuery({ queryKey: ['second'], queryFn: async () => null });
    expect(sink.close).toHaveBeenCalledTimes(1);
    await fail('first');
    expect(sink.show).toHaveBeenCalledTimes(2);
  });

  it('keeps an episode through refetch start, but resets on actual reset/removal', async () => {
    const { queryClient, sink } = setup();
    const fail = () =>
      queryClient
        .fetchQuery({ queryKey: ['episode'], queryFn: () => Promise.reject(networkError()) })
        .catch(() => undefined);
    await fail();
    let rejectPending!: (error: unknown) => void;
    const pending = queryClient
      .fetchQuery({
        queryKey: ['episode'],
        queryFn: () =>
          new Promise((_, reject) => {
            rejectPending = reject;
          }),
      })
      .catch(() => undefined);
    expect(sink.close).not.toHaveBeenCalled();
    rejectPending(networkError());
    await pending;
    expect(sink.show).toHaveBeenCalledTimes(1);
    await queryClient.resetQueries({ queryKey: ['episode'] });
    expect(sink.close).toHaveBeenCalledTimes(1);
    await fail();
    expect(sink.show).toHaveBeenCalledTimes(2);
    queryClient.removeQueries({ queryKey: ['episode'] });
    expect(sink.close).toHaveBeenCalledTimes(2);
  });

  it('ends old membership when a query changes its fault', async () => {
    const { queryClient, sink } = setup();
    for (const error of [networkError(), apiError(100500)]) {
      await queryClient
        .fetchQuery({ queryKey: ['change'], queryFn: () => Promise.reject(error) })
        .catch(() => undefined);
    }
    expect(sink.show).toHaveBeenCalledTimes(2);
    expect(sink.close).toHaveBeenCalledTimes(1);
  });

  it('preserves a live query across synthetic cleanup and destroys it after real disconnect', async () => {
    const runtime = createQueryFeedbackRuntime();
    const sink = { show: vi.fn(), close: vi.fn() };
    const disconnect = runtime.connect(sink);
    runtime.queryClient.setQueryData(['live'], 'cached');
    let resolvePending!: (value: string) => void;
    const pending = runtime.queryClient.fetchQuery({
      queryKey: ['live'],
      queryFn: () =>
        new Promise<string>((resolve) => {
          resolvePending = resolve;
        }),
    });
    disconnect();
    const disconnectAgain = runtime.connect(sink);
    await Promise.resolve();
    expect(runtime.queryClient.getQueryData(['live'])).toBe('cached');
    resolvePending('fresh');
    await pending;
    expect(runtime.queryClient.getQueryData(['live'])).toBe('fresh');
    disconnectAgain();
    await Promise.resolve();
    expect(runtime.queryClient.getQueryCache().getAll()).toHaveLength(0);
  });

  it('retains the save outcome summary alongside one deduplicated read fault', async () => {
    const { queryClient, sink } = setup();
    const observers = ['detail', 'list'].map((key) => {
      queryClient.setQueryData([key], []);
      const observer = new QueryObserver(queryClient, {
        queryKey: [key],
        staleTime: Infinity,
        queryFn: () => Promise.reject(networkError()),
        meta: { errorPresentation: 'local-initial' },
      });
      cleanups.push(observer.subscribe(() => undefined));
      return observer;
    });
    expect(observers).toHaveLength(2);
    const result = await runEditSavePostCommit({
      syncTab: () => undefined,
      refreshCache: () => queryClient.invalidateQueries({}, { throwOnError: true }),
    });
    expect(getEditSavePostCommitFeedback(result, false)).toEqual({
      type: 'warning',
      message: '保存已成功，但页面数据刷新失败',
    });
    expect(sink.show).toHaveBeenCalledTimes(1);
    await queryClient
      .fetchQuery({ queryKey: ['unrelated'], queryFn: () => Promise.reject(apiError(100500)) })
      .catch(() => undefined);
    expect(sink.show).toHaveBeenCalledTimes(2);
  });
});
