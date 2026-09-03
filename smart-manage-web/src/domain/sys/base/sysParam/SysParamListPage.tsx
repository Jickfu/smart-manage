import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useState } from 'react';
import { Button, Tag } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import ListTree from '@/domain/common/page/list/ListTree';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useListSelection } from '@/domain/common/page/list/useListSelection';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { featureApi } from '@/domain/sys/base/feature/api';
import { featureQueryKeys } from '@/domain/sys/base/feature/queryKeys';
import type { FeatureVO } from '@/domain/sys/base/feature/types';
import { useQuery } from '@tanstack/react-query';
import { sysParamApi } from './api';
import { sysParamAccess } from './permissions';
import { sysParamQueryKeys } from './queryKeys';
import type { SysParamVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  appName: { label: '所属应用', filter: { type: 'string' } },
  featureName: { label: '所属功能', filter: { type: 'string' } },
  value: { label: '参数值', filter: { type: 'string' } },
  description: { label: '描述', filter: { type: 'string' } },
  isSystem: { label: '类型', filter: { type: 'boolean' } },
};

const EDIT_KEY = componentKeys.sysParamEdit;

type Scope =
  | { type: 'all' }
  | { type: 'global' }
  | { type: 'domain'; id: string }
  | { type: 'app'; id: string }
  | { type: 'feature'; id: string };

const SysParamListPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [scope, setScope] = useState<Scope>({ type: 'all' });
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const treeQuery = useQuery({
    queryKey: appQueryKeys.domainAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });
  const featuresQuery = useQuery({
    queryKey: featureQueryKeys.visible(),
    queryFn: featureApi.listAllVisible,
    staleTime: 5 * 60 * 1000,
  });
  const scopeParams = {
    featureId: scope.type === 'feature' ? scope.id : undefined,
    appId: scope.type === 'app' ? scope.id : undefined,
    domainId: scope.type === 'domain' ? scope.id : undefined,
    globalOnly: scope.type === 'global' ? true : undefined,
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
    queryKey: sysParamQueryKeys.list(scopeParams),
    queryFn: (params) => sysParamApi.listPage({ ...params, ...scopeParams }),
  });
  const { selectedRowKeys, setSelectedRowKeys, selectedRecords, clearSelection } =
    useListSelection(records);
  const deleteMutation = useCommandMutation({
    mutationFn: sysParamApi.delete,
    onSuccess: async () => {
      clearSelection();
      await queryClient.invalidateQueries({ queryKey: sysParamQueryKeys.all });
      feedback.success('删除成功');
    },
  });
  const treeData = useMemo<DataNode[]>(() => {
    const featuresByApp = new Map<string, FeatureVO[]>();
    for (const feature of featuresQuery.data ?? []) {
      featuresByApp.set(feature.appId, [...(featuresByApp.get(feature.appId) ?? []), feature]);
    }
    return [
      {
        key: 'all',
        title: '全部参数',
        children: [
          { key: 'global', title: '全局参数', isLeaf: true },
          ...(treeQuery.data?.map((domain) => ({
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
          })) ?? []),
        ],
      },
    ];
  }, [featuresQuery.data, treeQuery.data]);
  const columns: ColumnsType<SysParamVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (text, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => openBillTab(props.appNumber, EDIT_KEY, record.id, OperationType.EDIT)}
        >
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: '所属应用', dataIndex: 'appName', width: 160, render: (value) => value ?? '全局参数' },
    { title: '所属功能', dataIndex: 'featureName', width: 160, render: (value) => value ?? '—' },
    { title: '参数值', dataIndex: 'value', ellipsis: true },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'isSystem',
      width: 100,
      render: (value) => (value ? <Tag color="blue">系统内置</Tag> : <Tag>自定义</Tag>),
    },
  ];
  return (
    <ListPage<SysParamVO>
      {...props}
      title="系统参数"
      access={sysParamAccess}
      loading={query.isLoading || treeQuery.isLoading || featuresQuery.isLoading}
      error={(query.error ?? treeQuery.error ?? featuresQuery.error) as Error | null}
      onRetry={() => Promise.all([query.refetch(), treeQuery.refetch(), featuresQuery.refetch()])}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      treePanel={
        <ListTreePanel>
          <ListTree
            virtual={false}
            treeData={treeData}
            blockNode
            defaultExpandedKeys={['all']}
            selectedKeys={[
              scope.type === 'all' || scope.type === 'global'
                ? scope.type
                : `${scope.type}:${scope.id}`,
            ]}
            onSelect={(keys) => {
              const key = String(keys[0] ?? 'all');
              clearSelection();
              resetPage();
              if (key === 'global') setScope({ type: 'global' });
              else if (key.startsWith('domain:'))
                setScope({ type: 'domain', id: key.slice('domain:'.length) });
              else if (key.startsWith('app:'))
                setScope({ type: 'app', id: key.slice('app:'.length) });
              else if (key.startsWith('feature:'))
                setScope({ type: 'feature', id: key.slice('feature:'.length) });
              else setScope({ type: 'all' });
            }}
          />
        </ListTreePanel>
      }
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY)}
      toolbarActions={[
        {
          key: 'delete',
          label: '删除',
          permission: sysParamAccess.permissions.delete,
          danger: true,
          disabled: selectedRowKeys.length !== 1 || selectedRecords[0]?.isSystem,
          loading: deleteMutation.isPending,
          onClick: () => {
            const record = selectedRecords[0];
            if (!record) return;
            void confirmOperation({
              type: 'delete',
              title: '确认删除系统参数？',
              description: record.number,
              confirmText: '删除',
              onConfirm: () => deleteMutation.mutateAsync(record.id),
            });
          },
        },
      ]}
      onRefresh={onRefresh}
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
  );
};

export default SysParamListPage;
