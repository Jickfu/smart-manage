import { useState, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { QueryKey } from '@tanstack/react-query';
import type { PaginationProps } from 'antd';
import type { PageData } from '@/types/api';
import type { ListFilterCondition, ListSortCondition } from './listQuery';
import { serializeListFilters } from './listQuery';

/** 标准列表查询参数 */
export interface ListPageQueryParams {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  filters?: string;
  sortField?: string;
  sortOrder?: 'ASC' | 'DESC';
}

/** 标准列表查询结果 */
export type ListPageQueryResult<T> = PageData<T>;

/** useListPageQuery 配置 */
interface UseListPageQueryOptions<T> {
  /** TanStack Query 的 queryKey 前缀，如 ['domain-list'] */
  queryKey: QueryKey;
  /** 查询函数，接收 ListPageQueryParams 返回 ListPageQueryResult */
  queryFn: (params: ListPageQueryParams) => Promise<ListPageQueryResult<T>>;
  /** 初始每页条数，默认 20 */
  initialPageSize?: number;
}

/**
 * 通用列表页查询 Hook — 封装分页、关键词搜索、刷新等行为。
 *
 * 约定：
 * - 查询参数变更自动触发 refetch（TanStack Query 基于 queryKey 缓存）
 * - 关键词搜索时自动重置到第 1 页
 * - 返回标准 antd Pagination 配置，直接传入 Table
 */
export function useListPageQuery<T>({
  queryKey,
  queryFn,
  initialPageSize = 20,
}: UseListPageQueryOptions<T>) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [keyword, setKeyword] = useState('');
  const [columnFilters, setColumnFilters] = useState<ListFilterCondition[]>([]);
  const [columnSort, setColumnSort] = useState<ListSortCondition>();
  const serializedFilters = serializeListFilters(columnFilters);

  const query = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: [...queryKey, pageNum, pageSize, keyword, serializedFilters, columnSort],
    queryFn: () =>
      queryFn({
        pageNum,
        pageSize,
        keyword: keyword || undefined,
        filters: serializedFilters,
        sortField: columnSort?.field,
        sortOrder: columnSort?.order,
      }),
  });

  const records = query.data?.records ?? [];
  const total = query.data?.total ?? 0;

  /** 关键词搜索 — 自动重置到第 1 页 */
  const onSearch = useCallback((value: string) => {
    setKeyword(value);
    setPageNum(1);
  }, []);

  /** 分页变更 */
  const onPageChange: PaginationProps['onChange'] = useCallback((nextPage, nextSize) => {
    setPageNum(nextPage);
    setPageSize(nextSize);
  }, []);

  /** 手动刷新 */
  const onRefresh = useCallback(() => {
    query.refetch();
  }, [query]);

  const resetPage = useCallback(() => setPageNum(1), []);

  const onColumnFiltersChange = useCallback((filters: ListFilterCondition[]) => {
    setColumnFilters(filters);
    setPageNum(1);
  }, []);

  const onColumnSortChange = useCallback((sort?: ListSortCondition) => {
    setColumnSort(sort);
    setPageNum(1);
  }, []);

  return {
    /** TanStack Query 原始结果（含 isLoading / isError / error 等） */
    query,
    records,
    total,
    pageNum,
    pageSize,
    keyword,
    columnFilters,
    columnSort,
    onSearch,
    onPageChange,
    onRefresh,
    resetPage,
    onColumnFiltersChange,
    onColumnSortChange,
    columnQueryProps: {
      columnFilters,
      columnSort,
      onColumnFiltersChange,
      onColumnSortChange,
    },
  };
}
