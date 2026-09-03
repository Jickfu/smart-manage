import { QueryCache, QueryClient, isCancelledError, type Query } from '@tanstack/react-query';
import { ApiError } from './ApiError';
import { getErrorPresentation, isErrorFeedbackSuppressed } from './errorPresentation';

export interface QueryErrorMeta extends Record<string, unknown> {
  errorPresentation?: 'local-initial' | 'local';
}

declare module '@tanstack/react-query' {
  interface Register {
    queryMeta: QueryErrorMeta;
  }
}

interface QueryErrorResult {
  data: unknown;
  error: unknown;
  isPlaceholderData?: boolean;
}

export function isResourceUnavailable(error: unknown): boolean {
  return (
    error instanceof ApiError &&
    (error.httpStatus === 403 ||
      error.httpStatus === 404 ||
      error.apiCode === 100403 ||
      error.apiCode === 100404)
  );
}

/** 空数组/null 是有效结果，placeholder 不是当前查询的成功数据。资源拒绝始终撤销操作能力。 */
export function getBlockingQueryError(result: QueryErrorResult): Error | null {
  if (!result.error || isCancelledError(result.error) || isErrorFeedbackSuppressed(result.error))
    return null;
  if (
    result.data !== undefined &&
    !result.isPlaceholderData &&
    !isResourceUnavailable(result.error)
  )
    return null;
  return result.error instanceof Error ? result.error : new Error('数据加载失败');
}

function ownsError(meta: QueryErrorMeta | undefined, result: QueryErrorResult): boolean {
  return (
    meta?.errorPresentation === 'local' ||
    (meta?.errorPresentation === 'local-initial' && getBlockingQueryError(result) !== null)
  );
}

/** 同一缓存可有多个 UI owner；不得依据最后写入的 query.meta 覆盖其他订阅者的职责。 */
function isLocallyOwned(query: Query<unknown, unknown>): boolean {
  const enabledObservers = query.observers.filter(
    (observer) => observer.getCurrentResult().isEnabled,
  );
  if (enabledObservers.length) {
    return enabledObservers.some((observer) =>
      ownsError(observer.options.meta, observer.getCurrentResult()),
    );
  }
  return ownsError(query.meta, query.state);
}

interface QueryFeedbackSink {
  show: (error: unknown, key: string) => void;
  close: (key: string) => void;
}

interface Fault {
  key: string;
  members: Set<string>;
  error: unknown;
  presented: boolean;
  shouldPresent: boolean;
}

/** 每个应用 QueryClient 自己拥有故障状态；没有模块级 UI 回调，也不保存错误历史。 */
export function createQueryFeedbackRuntime() {
  const runtimeId = crypto.randomUUID();
  const faults = new Map<string, Fault>();
  const memberships = new Map<string, string>();
  let sink: QueryFeedbackSink | undefined;
  let sequence = 0;
  let generation = 0;

  const release = (queryHash: string) => {
    const fingerprint = memberships.get(queryHash);
    if (!fingerprint) return;
    memberships.delete(queryHash);
    const fault = faults.get(fingerprint);
    if (!fault) return;
    fault.members.delete(queryHash);
    if (!fault.members.size) {
      sink?.close(fault.key);
      faults.delete(fingerprint);
    }
  };

  const present = (fault: Fault) => {
    if (sink && fault.shouldPresent && !fault.presented) {
      fault.presented = true;
      sink.show(fault.error, fault.key);
    }
  };

  const queryCache = new QueryCache({
    onError: (error, query) => {
      if (isCancelledError(error) || isErrorFeedbackSuppressed(error)) return;
      const presentation = getErrorPresentation(error, '数据加载失败，请稍后重试');
      const fingerprint = JSON.stringify([
        presentation.source,
        error instanceof ApiError ? error.httpStatus : null,
        error instanceof ApiError ? error.apiCode : null,
        presentation.type,
        presentation.message,
      ]);
      if (memberships.get(query.queryHash) !== fingerprint) release(query.queryHash);
      let fault = faults.get(fingerprint);
      if (!fault) {
        fault = {
          key: `sm-query-fault-${runtimeId}-${++sequence}`,
          members: new Set(),
          error,
          presented: false,
          shouldPresent: false,
        };
        faults.set(fingerprint, fault);
      }
      memberships.set(query.queryHash, fingerprint);
      fault.members.add(query.queryHash);
      fault.error = error;
      fault.shouldPresent ||= !isLocallyOwned(query);
      present(fault);
    },
    onSuccess: (_data, query) => release(query.queryHash),
  });

  // resetQueries 恢复初始 state，不存在单独的 reset 事件；开始 refetch 不能结束故障。
  queryCache.subscribe((event) => {
    if (
      event.type === 'removed' ||
      (event.type === 'updated' &&
        event.action.type === 'setState' &&
        event.query.state.error === null)
    ) {
      release(event.query.queryHash);
    }
  });

  const queryClient = new QueryClient({
    queryCache,
    defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: false } },
  });

  return {
    queryClient,
    connect(nextSink: QueryFeedbackSink) {
      generation += 1;
      sink = nextSink;
      for (const fault of faults.values()) present(fault);
      return () => {
        sink = undefined;
        const disconnectedGeneration = ++generation;
        // StrictMode 紧接着的 setup 会取消销毁；真实卸载才清理缓存、故障与该 runtime 的提示。
        queueMicrotask(() => {
          if (generation !== disconnectedGeneration) return;
          for (const fault of faults.values()) nextSink.close(fault.key);
          queryClient.clear();
          faults.clear();
          memberships.clear();
        });
      };
    },
  };
}
