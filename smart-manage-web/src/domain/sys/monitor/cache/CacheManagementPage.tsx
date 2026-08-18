import { useMemo, useState } from 'react';
import type { Key } from 'react';
import { App, Button, Tag, Tree } from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { useWorkbenchStore } from '@/stores/workbench';
import { cacheApi } from './api';
import { cacheAccess } from './permissions';
import { cacheQueryKeys } from './queryKeys';
import { scopeNodeKey, scopeNodeKeyFromFilter, toTreeNode } from './scopeTree';
import type { CacheEntry, CacheEntryKey, CacheScopeFilter } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

const columnFeatures: ListColumnFeatures = {
  key: { label: 'Key', filter: { type: 'string' }, sorter: true },
  cacheDisplayName: { label: '缓存', filter: { type: 'string' } },
  storage: {
    label: '存储位置',
    filter: {
      type: 'enum',
      options: [
        { label: '本地', value: 'LOCAL' },
        { label: 'Redis', value: 'REDIS' },
      ],
    },
  },
  type: { label: '类型', filter: { type: 'string' } },
  ttl: { label: 'TTL', filter: { type: 'number' }, sorter: true },
  memoryBytes: { label: '内存', filter: { type: 'number' }, sorter: true },
};

function entryKey(entry: CacheEntry): CacheEntryKey {
  return { storage: entry.storage, cacheName: entry.cacheName, key: entry.key };
}

const CACHE_VALUE_COMPONENT = 'sys/monitor/cache-value';
const ALL_SCOPE: CacheScopeFilter = { scopeType: 'ALL' };

export default function CacheManagementPage(props: PageComponentProps) {
  const { modal } = App.useApp();
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const { can } = usePermissionAccess(cacheAccess.prefix);
  const queryClient = useQueryClient();
  const [scope, setScope] = useState<CacheScopeFilter>(ALL_SCOPE);
  const [scopeLabel, setScopeLabel] = useState('全部缓存');
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const scopeTreeQuery = useQuery({
    queryKey: cacheQueryKeys.scopeTree(),
    queryFn: cacheApi.scopeTree,
  });
  const list = useListPageQuery({
    queryKey: cacheQueryKeys.entries(scope),
    queryFn: (params) => cacheApi.listPage({ ...params, ...scope }),
  });
  const treeData = useMemo<DataNode[]>(() => {
    return [
      {
        key: 'all',
        title: '全部缓存',
        children: (scopeTreeQuery.data ?? []).map(toTreeNode),
      },
    ];
  }, [scopeTreeQuery.data]);
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
      render: (key, record) => {
        if (!record.valueReadable || !can(cacheAccess.permissions.value)) {
          return <span>{key}</span>;
        }
        return (
          <Button
            type="link"
            size="small"
            onClick={() =>
              openBillTab(
                props.appNumber,
                CACHE_VALUE_COMPONENT,
                record.identity,
                OperationType.VIEW,
              )
            }
          >
            {key}
          </Button>
        );
      },
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
        filterSummary={scope.scopeType === 'ALL' ? undefined : scopeLabel}
        treePanel={
          <ListTreePanel>
            <Tree
              virtual={false}
              key={scopeTreeQuery.data ? 'scope-tree-loaded' : 'scope-tree-loading'}
              blockNode
              treeData={treeData}
              defaultExpandedKeys={[
                'all',
                ...(scopeTreeQuery.data ?? [])
                  .filter((item) => item.type === 'CLOUD')
                  .map(scopeNodeKey),
              ]}
              selectedKeys={[scopeNodeKeyFromFilter(scope)]}
              onSelect={(_, info) => {
                const selectedKey = String(info.node.key);
                if (selectedKey.startsWith('app:')) {
                  const [, cloudNumber, appNumber] = selectedKey.split(':');
                  setScope({ scopeType: 'APP', cloudNumber, appNumber });
                } else if (selectedKey.startsWith('cloud:')) {
                  setScope({ scopeType: 'CLOUD', cloudNumber: selectedKey.slice('cloud:'.length) });
                } else if (selectedKey === 'other') {
                  setScope({ scopeType: 'OTHER' });
                } else {
                  setScope(ALL_SCOPE);
                }
                setScopeLabel(String(info.node.title ?? '全部缓存'));
                list.resetPage();
                setSelectedRowKeys([]);
              }}
            />
          </ListTreePanel>
        }
        toolbarActions={[
          {
            key: 'delete',
            label: '删除所选',
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
        columnFeatures={columnFeatures}
        {...list.columnQueryProps}
        dataSource={list.records}
        selectMode="checkbox"
        selectedRowKeys={selectedRowKeys}
        onSelectChange={setSelectedRowKeys}
      />
    </>
  );
}
