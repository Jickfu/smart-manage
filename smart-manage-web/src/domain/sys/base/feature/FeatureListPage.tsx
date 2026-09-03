import { useMemo, useState } from 'react';
import { Button, Tag } from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTree from '@/domain/common/page/list/ListTree';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { featureApi } from './api';
import FeatureEditPage from './FeatureEditPage';
import { featureAccess } from './permissions';
import { featureQueryKeys } from './queryKeys';
import type { FeatureVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  featureKey: { label: '功能键', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  appName: { label: '所属应用', filter: { type: 'string' } },
  source: {
    label: '来源',
    filter: {
      type: 'enum',
      options: [
        { label: '系统', value: 'SYSTEM' },
        { label: '插件', value: 'PLUGIN' },
        { label: '外部', value: 'EXTERNAL' },
      ],
    },
  },
  seq: { label: '排序', filter: { type: 'number' }, sorter: true },
  visible: { label: '目录状态', filter: { type: 'boolean' } },
};

type Scope = { type: 'all' } | { type: 'domain'; id: string } | { type: 'app'; id: string };

const FeatureListPage = (props: PageComponentProps) => {
  const [scope, setScope] = useState<Scope>({ type: 'all' });
  const [editId, setEditId] = useState<string | null>(null);
  const treeQuery = useQuery({ queryKey: appQueryKeys.domainAppsAll(), queryFn: fetchAppsAll });
  const scopeParams = {
    domainId: scope.type === 'domain' ? scope.id : undefined,
    appId: scope.type === 'app' ? scope.id : undefined,
  };
  const {
    records,
    total,
    pageNum,
    pageSize,
    keyword,
    query,
    onSearch,
    onPageChange,
    onRefresh,
    resetPage,
    columnQueryProps,
  } = useListPageQuery({
    queryKey: featureQueryKeys.list(scopeParams),
    queryFn: (params) => featureApi.listPage({ ...params, ...scopeParams }),
  });
  const treeData = useMemo<DataNode[]>(
    () => [
      {
        key: 'all',
        title: '全部应用',
        children:
          treeQuery.data?.map((domain) => ({
            key: `domain:${domain.id}`,
            title: domain.name,
            children: domain.appList.map((application) => ({
              key: `app:${application.id}`,
              title: application.name,
              isLeaf: true,
            })),
          })) ?? [],
      },
    ],
    [treeQuery.data],
  );
  const columns: ColumnsType<FeatureVO> = [
    {
      title: '功能键',
      dataIndex: 'featureKey',
      width: 260,
      render: (value, record) => (
        <Button type="link" size="small" onClick={() => setEditId(record.id)}>
          {value}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name' },
    { title: '所属应用', dataIndex: 'appName', width: 140 },
    { title: '来源', dataIndex: 'source', width: 90 },
    { title: '排序', dataIndex: 'seq', width: 70 },
    {
      title: '目录状态',
      dataIndex: 'visible',
      width: 90,
      render: (value) => (value ? <Tag color="green">可见</Tag> : <Tag>隐藏</Tag>),
    },
  ];
  return (
    <>
      <ListPage<FeatureVO>
        {...props}
        title="功能"
        access={featureAccess}
        loading={query.isLoading || treeQuery.isLoading}
        error={(query.error ?? treeQuery.error) as Error | null}
        onRetry={() => Promise.all([query.refetch(), treeQuery.refetch()])}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索功能键/名称"
        filterSummary={keyword ? `关键字：${keyword}` : undefined}
        treePanel={
          <ListTreePanel>
            <ListTree
              virtual={false}
              blockNode
              treeData={treeData}
              defaultExpandedKeys={['all']}
              selectedKeys={[scope.type === 'all' ? 'all' : `${scope.type}:${scope.id}`]}
              onSelect={(keys) => {
                const key = String(keys[0] ?? 'all');
                resetPage();
                if (key.startsWith('domain:'))
                  setScope({ type: 'domain', id: key.slice('domain:'.length) });
                else if (key.startsWith('app:'))
                  setScope({ type: 'app', id: key.slice('app:'.length) });
                else setScope({ type: 'all' });
              }}
            />
          </ListTreePanel>
        }
        onRefresh={onRefresh}
        onQuickSearch={onSearch}
        onPageChange={onPageChange}
        rowKey="id"
        columns={columns}
        columnFeatures={columnFeatures}
        {...columnQueryProps}
        dataSource={records}
      />
      <FeatureEditPage open={Boolean(editId)} featureId={editId} onClose={() => setEditId(null)} />
    </>
  );
};

export default FeatureListPage;
