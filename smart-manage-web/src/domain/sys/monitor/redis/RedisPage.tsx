import { useState } from 'react';
import type { Key } from 'react';
import { App, Alert, Button, Drawer, Select, Space, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { cacheApi } from '../cache/api';
import { cacheAccess } from '../cache/permissions';
import { cacheQueryKeys } from '../cache/queryKeys';
import type { CacheEntry, CacheEntryKey } from '../cache/types';
import './redisPage.css';

function entryKey(entry: CacheEntry): CacheEntryKey {
  return { storage: entry.storage, cacheName: entry.cacheName, key: entry.key };
}

export default function RedisPage(_: PageComponentProps) {
  const { modal } = App.useApp();
  const queryClient = useQueryClient();
  const [storage, setStorage] = useState<string>();
  const [cacheName, setCacheName] = useState<string>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [valueEntry, setValueEntry] = useState<CacheEntry>();
  const catalogQuery = useQuery({
    queryKey: cacheQueryKeys.overview(),
    queryFn: cacheApi.overview,
  });
  const list = useListPageQuery({
    queryKey: cacheQueryKeys.entries(storage, cacheName),
    queryFn: (params) => cacheApi.listPage({ ...params, storage, cacheName }),
  });
  const valueQuery = useQuery({
    queryKey: cacheQueryKeys.value(valueEntry?.identity),
    queryFn: () => cacheApi.value(entryKey(valueEntry!)),
    enabled: Boolean(valueEntry),
  });
  const deleteMutation = useCommandMutation({
    mutationFn: cacheApi.delete,
    successMessage: (entries) => `已删除 ${entries.length} 个缓存条目`,
    onSuccess: async () => {
      setSelectedRowKeys([]);
      await queryClient.invalidateQueries({ queryKey: cacheQueryKeys.all });
    },
  });
  const clearAllMutation = useCommandMutation({
    mutationFn: cacheApi.clearAll,
    successMessage: '全部已登记应用缓存已清理',
    onSuccess: () => queryClient.invalidateQueries({ queryKey: cacheQueryKeys.all }),
  });
  const selectedEntries = list.records.filter((entry) => selectedRowKeys.includes(entry.identity));
  const confirmDelete = (entries: CacheEntry[]) =>
    modal.confirm({
      title: `删除 ${entries.length} 个缓存条目？`,
      content: '删除后无法恢复，请确认这些缓存可以安全重建。',
      okText: '确认删除',
      okButtonProps: { danger: true },
      onOk: () => deleteMutation.mutateAsync(entries.map(entryKey)),
    });
  const columns: ColumnsType<CacheEntry> = [
    {
      title: 'Key',
      dataIndex: 'key',
      ellipsis: true,
      render: (key, record) => (
        <Button
          type="link"
          size="small"
          disabled={!record.valueReadable}
          onClick={() => setValueEntry(record)}
        >
          {key}
        </Button>
      ),
    },
    { title: '缓存', dataIndex: 'cacheDisplayName', width: 140 },
    {
      title: '存储位置',
      dataIndex: 'storage',
      width: 110,
      render: (value) => <Tag color={value === 'LOCAL' ? 'blue' : 'purple'}>{value}</Tag>,
    },
    { title: '类型', dataIndex: 'type', width: 100 },
    {
      title: 'TTL',
      dataIndex: 'ttl',
      width: 110,
      render: (value) =>
        value == null ? '-' : value === -1 ? '永久' : value === -2 ? '不存在' : `${value} 秒`,
    },
    {
      title: '内存',
      dataIndex: 'memoryBytes',
      width: 110,
      render: (value) => (value == null ? '-' : `${value} B`),
    },
    {
      title: 'Value',
      dataIndex: 'valueReadable',
      width: 100,
      render: (value) => (value ? '可查看' : <Tag color="warning">敏感</Tag>),
    },
    {
      title: '操作',
      key: 'action',
      width: 90,
      render: (_, record) => (
        <PermissionActions
          prefix={cacheAccess.prefix}
          actions={[
            {
              key: 'delete',
              label: '删除',
              permission: cacheAccess.permissions.delete,
              danger: true,
              onClick: () => confirmDelete([record]),
            },
          ]}
        />
      ),
    },
  ];
  return (
    <>
      <ListPage<CacheEntry>
        title="缓存管理"
        access={cacheAccess}
        loading={list.query.isLoading}
        error={list.query.error as Error | null}
        onRetry={() => list.query.refetch()}
        total={list.total}
        pageNum={list.pageNum}
        pageSize={list.pageSize}
        quickSearchPlaceholder="搜索 Key 或缓存名称"
        filterSummary={[storage, cacheName].filter(Boolean).join(' / ') || undefined}
        filterContent={
          <Space wrap>
            <Select
              allowClear
              placeholder="存储位置"
              value={storage}
              options={[
                { label: '本地缓存', value: 'LOCAL' },
                { label: 'Redis', value: 'REDIS' },
              ]}
              onChange={(value) => {
                setStorage(value);
                setSelectedRowKeys([]);
              }}
            />
            <Select
              allowClear
              placeholder="应用缓存"
              value={cacheName}
              options={catalogQuery.data?.caches.map((cache) => ({
                label: cache.displayName,
                value: cache.name,
              }))}
              onChange={(value) => {
                setCacheName(value);
                setSelectedRowKeys([]);
              }}
            />
          </Space>
        }
        toolbarActions={[
          {
            key: 'delete',
            label: `删除所选（${selectedRowKeys.length}）`,
            permission: cacheAccess.permissions.delete,
            danger: true,
            disabled: selectedRowKeys.length === 0,
            loading: deleteMutation.isPending,
            onClick: () => confirmDelete(selectedEntries),
          },
          {
            key: 'clearAll',
            label: '清空全部应用缓存',
            permission: cacheAccess.permissions.clearAll,
            danger: true,
            loading: clearAllMutation.isPending,
            onClick: () =>
              modal.confirm({
                title: '清空全部已登记应用缓存？',
                content: '包含当前节点本地缓存和共享 Redis 应用缓存；不会执行 FLUSHDB。',
                okText: '确认清空',
                okButtonProps: { danger: true },
                onOk: () => clearAllMutation.mutateAsync(),
              }),
          },
        ]}
        onRefresh={list.onRefresh}
        onQuickSearch={list.onSearch}
        onPageChange={list.onPageChange}
        rowKey="identity"
        columns={columns}
        dataSource={list.records}
        selectMode="checkbox"
        selectedRowKeys={selectedRowKeys}
        onSelectChange={setSelectedRowKeys}
      />
      <Drawer
        title={valueEntry ? `缓存值：${valueEntry.key}` : '缓存值'}
        size="large"
        open={Boolean(valueEntry)}
        destroyOnHidden
        onClose={() => setValueEntry(undefined)}
      >
        {valueQuery.error && <Alert type="error" showIcon title={valueQuery.error.message} />}
        {valueQuery.data?.truncated && (
          <Alert type="warning" showIcon title="内容过大，仅展示安全上限内的数据。" />
        )}
        <pre className="sm-redis-value">
          {valueQuery.isLoading ? '加载中…' : JSON.stringify(valueQuery.data?.items ?? [], null, 2)}
        </pre>
      </Drawer>
    </>
  );
}
