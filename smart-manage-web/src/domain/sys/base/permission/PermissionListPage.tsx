import { useCallback, useMemo, useState } from 'react';
import { Button } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTree from '@/domain/common/page/list/ListTree';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import type { PageComponentProps } from '@/domain/common/page/types';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { featureApi } from '@/domain/sys/base/feature/api';
import { featureQueryKeys } from '@/domain/sys/base/feature/queryKeys';
import type { FeatureVO } from '@/domain/sys/base/feature/types';
import PermissionEditPage from './PermissionEditPage';
import { permissionApi } from './api';
import { permissionAccess } from './permissions';
import { permissionQueryKeys } from './queryKeys';
import type { PermissionListVO } from './types';
import { usePermissionDeleteMutation } from './usePermissionDeleteMutation';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  featureName: { label: '所属功能', filter: { type: 'string' } },
  appName: { label: '所属应用', filter: { type: 'string' } },
};

type PermissionScope =
  | { type: 'all' }
  | { type: 'domain'; id: string }
  | { type: 'app'; id: string }
  | { type: 'feature'; id: string };

const PermissionListPage = (props: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const { can } = usePermissionAccess(permissionAccess.prefix);
  const [scope, setScope] = useState<PermissionScope>({ type: 'all' });
  const appsQuery = useQuery({ queryKey: appQueryKeys.domainAppsAll(), queryFn: fetchAppsAll });
  const featuresQuery = useQuery({
    queryKey: featureQueryKeys.visible(),
    queryFn: featureApi.listAllVisible,
  });
  const scopeParams = {
    appId: scope.type === 'app' ? scope.id : undefined,
    domainId: scope.type === 'domain' ? scope.id : undefined,
    featureId: scope.type === 'feature' ? scope.id : undefined,
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
    queryKey: permissionQueryKeys.list(scopeParams),
    queryFn: (params) => permissionApi.listPage({ ...params, ...scopeParams }),
  });
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [editId, setEditId] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const deleteMutation = usePermissionDeleteMutation(async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });
  const openEdit = useCallback((id: string | null) => {
    setEditId(id);
    setModalOpen(true);
  }, []);
  const columns: ColumnsType<PermissionListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 240,
      render: (text, record) =>
        can(permissionAccess.permissions.detail) ? (
          <Button type="link" size="small" onClick={() => openEdit(record.id)}>
            {text}
          </Button>
        ) : (
          text
        ),
    },
    { title: '名称', dataIndex: 'name' },
    {
      title: '所属功能',
      dataIndex: 'featureName',
      width: 160,
      render: (value) => value ?? '应用级权限',
    },
    { title: '所属应用', dataIndex: 'appName', width: 160 },
  ];
  const treeData = useMemo<DataNode[]>(() => {
    const featuresByApp = new Map<string, FeatureVO[]>();
    for (const feature of featuresQuery.data ?? []) {
      featuresByApp.set(feature.appId, [...(featuresByApp.get(feature.appId) ?? []), feature]);
    }
    return [
      {
        key: 'all',
        title: '全部功能',
        children: (appsQuery.data ?? []).map((domain) => ({
          key: `domain:${domain.id}`,
          title: domain.name,
          children: domain.appList.map((application) => ({
            key: `app:${application.id}`,
            title: application.name,
            children: (featuresByApp.get(application.id) ?? []).map((feature) => ({
              key: `feature:${feature.id}`,
              title: feature.name,
              isLeaf: true,
            })),
          })),
        })),
      },
    ];
  }, [appsQuery.data, featuresQuery.data]);

  return (
    <>
      <ListPage<PermissionListVO>
        {...props}
        title="权限定义"
        access={permissionAccess}
        loading={query.isLoading || appsQuery.isLoading || featuresQuery.isLoading}
        error={(query.error ?? appsQuery.error ?? featuresQuery.error) as Error | null}
        onRetry={() => Promise.all([query.refetch(), appsQuery.refetch(), featuresQuery.refetch()])}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索编码/名称"
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
                const [type, id] = String(keys[0] ?? 'all').split(':');
                setSelectedRowKeys([]);
                resetPage();
                if (type === 'domain' && id) setScope({ type: 'domain', id });
                else if (type === 'app' && id) setScope({ type: 'app', id });
                else if (type === 'feature' && id) setScope({ type: 'feature', id });
                else setScope({ type: 'all' });
              }}
            />
          </ListTreePanel>
        }
        onAddNew={() => openEdit(null)}
        onDelete={() => {
          if (selectedRowKeys.length === 0) return;
          void confirmOperation({
            type: 'delete',
            title: '确认删除',
            description: `确定要删除选中的 ${selectedRowKeys.length} 条记录吗？`,
            confirmText: '删除',
            cancelText: '取消',
            onConfirm: () => deleteMutation.mutateAsync(selectedRowKeys.map(String)),
          });
        }}
        onRefresh={async () => {
          await Promise.all([onRefresh(), appsQuery.refetch(), featuresQuery.refetch()]);
        }}
        onQuickSearch={onSearch}
        onPageChange={onPageChange}
        rowKey="id"
        columns={columns}
        columnFeatures={columnFeatures}
        {...columnQueryProps}
        dataSource={records}
        selectMode="checkbox"
        selectedRowKeys={selectedRowKeys}
        onSelectChange={setSelectedRowKeys}
      />
      <PermissionEditPage
        open={modalOpen}
        permissionId={editId}
        onClose={() => setModalOpen(false)}
        onSaved={() => {
          setModalOpen(false);
          query.refetch();
        }}
      />
    </>
  );
};

export default PermissionListPage;
