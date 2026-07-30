import { useCallback, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import type { QueryKey } from '@tanstack/react-query';
import type { PageData } from '@/types/api';
import type { AuditLogFilters, AuditLogListParams } from './types';

interface Options<T, TFilters extends AuditLogFilters> {
  queryKey: (params: AuditLogListParams & Omit<TFilters, 'timeRange'>) => QueryKey;
  queryFn: (params: AuditLogListParams & Omit<TFilters, 'timeRange'>) => Promise<PageData<T>>;
}

export function useAuditLogListQuery<T, TFilters extends AuditLogFilters>({
  queryKey,
  queryFn,
}: Options<T, TFilters>) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const [filters, setFilters] = useState<TFilters>({} as TFilters);

  const { timeRange, ...filterValues } = filters;
  const params = {
    pageNum,
    pageSize,
    keyword: keyword || undefined,
    ...filterValues,
    traceId: filters.traceId?.trim() || undefined,
    beginTime: timeRange?.[0].format('YYYY-MM-DDTHH:mm:ss'),
    endTime: timeRange?.[1].format('YYYY-MM-DDTHH:mm:ss'),
  };

  const query = useQuery({
    queryKey: queryKey(params),
    queryFn: () => queryFn(params),
  });

  const onQuickSearch = useCallback((value: string) => {
    setKeyword(value.trim());
    setPageNum(1);
  }, []);

  const onFilter = useCallback((values: TFilters) => {
    setFilters(values);
    setPageNum(1);
  }, []);

  const onPageChange = useCallback((nextPage: number, nextPageSize: number) => {
    setPageNum(nextPage);
    setPageSize(nextPageSize);
  }, []);

  return {
    query,
    records: query.data?.records ?? [],
    total: query.data?.total ?? 0,
    pageNum,
    pageSize,
    keyword,
    filters,
    onQuickSearch,
    onFilter,
    onPageChange,
    onRefresh: () => query.refetch(),
  };
}
