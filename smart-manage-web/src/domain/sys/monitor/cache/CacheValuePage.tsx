import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { cacheApi } from './api';
import { parseCacheEntryIdentity } from './entryIdentity';
import { cacheQueryKeys } from './queryKeys';

const fields: EditField[] = [
  { label: '存储位置', dataIndex: 'storage', type: 'text' },
  { label: '应用缓存', dataIndex: 'cacheName', type: 'text' },
  { label: '数据类型', dataIndex: 'type', type: 'text' },
  { label: '是否截断', dataIndex: 'truncatedLabel', type: 'text' },
  { label: 'Key', dataIndex: 'key', type: 'textarea', fullWidth: true },
  { label: 'Value', dataIndex: 'value', type: 'textarea', fullWidth: true },
];

export default function CacheValuePage(props: PageComponentProps) {
  const entry = useMemo(() => parseCacheEntryIdentity(props.billId ?? ''), [props.billId]);
  const detailQuery = useQuery({
    queryKey: cacheQueryKeys.value(props.billId),
    queryFn: () => cacheApi.value(entry),
    enabled: Boolean(props.billId),
  });
  const initialValues = useMemo(
    () => ({
      ...entry,
      cacheName: entry.cacheName ?? '未登记 Redis Key',
      type: detailQuery.data?.type,
      truncatedLabel: detailQuery.data?.truncated ? '是' : '否',
      value: detailQuery.data ? JSON.stringify(detailQuery.data.items, null, 2) : '',
    }),
    [detailQuery.data, entry],
  );

  return (
    <EditPage
      title="缓存值"
      fields={fields}
      initialValues={initialValues}
      operationType={OperationType.VIEW}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
