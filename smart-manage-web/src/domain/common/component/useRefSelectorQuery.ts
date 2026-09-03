import { useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { isErrorFeedbackSuppressed } from '@/api/errorPresentation';

/** RefSelector 查询参数 — 与后端 listPage 接口对齐 */
export interface RefSelectorQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  /** 树表模式下选中的树节点ID，用于过滤表格数据 */
  parentId?: string;
}

/** RefSelector 查询结果 */
export interface RefSelectorQueryResult<T> {
  records: T[];
  total: number;
}

/** RefSelector 数据获取函数签名 */
export type RefSelectorFetchFn<T> = (
  params: RefSelectorQueryParams,
) => Promise<RefSelectorQueryResult<T>>;

interface UseRefSelectorQueryOptions<T> {
  fetchFn: RefSelectorFetchFn<T>;
  /** 选择器标识，用于隔离不同实例的查询缓存。简单场景传 'sys-domain'，复杂场景传 ['sys-org-user', orgId] */
  selectorKey: string | readonly unknown[];
  initialPageSize?: number;
  /** 控制是否触发请求，通常绑定 modalOpen */
  enabled: boolean;
}

/**
 * RefSelector 数据查询 hook。
 *
 * 基于 TanStack Query 管理请求状态，避免手动 useEffect + setState。
 * fetchFn 签名与后端 listPage API 兼容，可直接复用。
 */
export function useRefSelectorQuery<T>({
  fetchFn,
  selectorKey,
  initialPageSize = 20,
  enabled,
}: UseRefSelectorQueryOptions<T>) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [keyword, setKeyword] = useState('');
  const [parentId, setParentId] = useState<string | undefined>(undefined);

  /** 参数变更自动触发 refetch（TanStack Query 基于 queryKey 缓存） */
  const query = useQuery({
    meta: { errorPresentation: 'local' },
    queryKey: ['ref-selector', selectorKey, pageNum, pageSize, keyword, parentId],
    queryFn: () =>
      fetchFn({
        pageNum,
        pageSize,
        keyword: keyword || undefined,
        parentId,
      }),
    enabled,
    // 每次打开 Modal 都重新拉取数据，不使用缓存
    staleTime: 0,
  });

  const records = query.data?.records ?? [];
  const total = query.data?.total ?? 0;

  /** 关键字搜索（重置到第一页） */
  const onSearch = useCallback((value: string) => {
    setKeyword(value);
    setPageNum(1);
  }, []);

  /** 分页切换 */
  const onPageChange = useCallback((nextPage: number, nextSize: number) => {
    setPageNum(nextPage);
    setPageSize(nextSize);
  }, []);

  /** 树节点选中（重置到第一页），传 undefined 表示清除过滤 */
  const onTreeSelect = useCallback((id: string | undefined) => {
    setParentId(id);
    setPageNum(1);
  }, []);

  /** 重置搜索/分页状态（Modal 每次打开时调用，恢复初始状态） */
  const reset = useCallback(() => {
    setPageNum(1);
    setPageSize(initialPageSize);
    setKeyword('');
    setParentId(undefined);
  }, [initialPageSize]);

  return {
    records,
    total,
    pageNum,
    pageSize,
    keyword,
    parentId,
    /** 初始加载中（无缓存数据） */
    loading: query.isLoading,
    /** 后台刷新中（有缓存数据） */
    fetching: query.isFetching,
    error: isErrorFeedbackSuppressed(query.error) ? null : query.error,
    onSearch,
    onPageChange,
    onTreeSelect,
    reset,
    refresh: query.refetch,
  };
}
