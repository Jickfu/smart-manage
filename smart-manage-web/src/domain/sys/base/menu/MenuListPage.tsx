import { useState, useMemo, useCallback } from 'react';
import { App, Button, Tag, Tree } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useMenuDeleteMutation } from './useMenuDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { OperationType } from '@/domain/common/page/types';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { menuApi } from './api';
import { menuQueryKeys } from './queryKeys';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import type { MenuListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { menuAccess } from './permissions';

/** 菜单编辑页 componentKey */
const MENU_EDIT_KEY = 'sys/base/menu/edit';

/** 树节点 key 格式：cloud:{cloudId} | app:{appId} */
function nodeKey(prefix: string, id: string) {
  return `${prefix}:${id}`;
}

/** 菜单列表页 — 左树右表 */
const MenuListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const [selectedNodeKey, setSelectedNodeKey] = useState<string | undefined>(undefined);
  // 当前选中的应用节点；云节点仅用于展开，不作为列表过滤条件。
  const [selectedFilter, setSelectedFilter] = useState<{ type: string; id: string } | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((s) => s.openBillTab);
  const openAddNewTab = useWorkbenchStore((s) => s.openAddNewTab);

  // 左树只加载云和应用，菜单数据统一在右侧分页列表中展示。
  const appsQuery = useQuery({
    queryKey: appQueryKeys.cloudAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });

  // 右侧列表
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange } =
    useListPageQuery({
      queryKey: menuQueryKeys.list({
        type: selectedFilter?.type ?? '',
        id: selectedFilter?.id ?? '',
      }),
      queryFn: (params) => {
        const filter: { appId?: string; parentId?: string } = {};
        if (selectedFilter?.type === 'app') filter.appId = selectedFilter.id;
        return menuApi.listPage({ ...params, ...filter });
      },
    });
  const deleteMutation = useMenuDeleteMutation(async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });
  const enabledMutation = useEnabledMutation(menuApi.setEnabled, async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });

  const handleRefresh = useCallback(async () => {
    // 菜单管理是左树右表聚合页面，手动刷新必须同步更新两侧数据。
    await Promise.all([appsQuery.refetch(), query.refetch()]);
  }, [appsQuery, query]);

  // 构建左侧树
  const treeData: DataNode[] = useMemo(() => {
    if (!appsQuery.data) return [];
    const nodes: DataNode[] = [];
    for (const cloud of appsQuery.data) {
      const cloudChildren: DataNode[] = [];
      for (const app of cloud.appList) {
        const appNode: DataNode = {
          key: nodeKey('app', app.id),
          title: app.name,
          isLeaf: true,
        };
        cloudChildren.push(appNode);
      }
      if (cloudChildren.length > 0) {
        nodes.push({
          key: nodeKey('cloud', cloud.id),
          title: cloud.name,
          children: cloudChildren,
        });
      }
    }
    return nodes;
  }, [appsQuery.data]);

  const handleTreeSelect = useCallback((keys: React.Key[]) => {
    if (keys.length === 0) {
      setSelectedFilter(null);
      setSelectedNodeKey(undefined);
      return;
    }
    const key = String(keys[0]);
    setSelectedNodeKey(key);

    const [type, id] = key.split(':') as [string, string];
    if (type === 'app') {
      setSelectedFilter({ type: 'app', id });
    } else {
      // 云节点用于展开应用，不附加菜单过滤条件。
      setSelectedFilter(null);
    }
  }, []);

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, MENU_EDIT_KEY, '编辑菜单', id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, MENU_EDIT_KEY, '新增菜单');
  }, [props.appNumber, openAddNewTab]);

  const handleDelete = useCallback(() => {
    if (selectedRowKeys.length === 0) return;
    modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 条记录吗？`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => deleteMutation.mutateAsync(selectedRowKeys.map(String)),
    });
  }, [selectedRowKeys, deleteMutation, modal]);

  const columns: ColumnsType<MenuListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 120,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text || '-'}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 180 },
    {
      title: '层级',
      dataIndex: 'level',
      width: 80,
      render: (val) => (val === 2 ? <Tag color="blue">分组</Tag> : <Tag color="green">页面</Tag>),
    },
    { title: '路径', dataIndex: 'path', width: 180, ellipsis: true },
    { title: '组件', dataIndex: 'component', ellipsis: true },
    { title: '排序', dataIndex: 'sort', width: 60 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (value) => (value ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
    },
  ];

  const loading = query.isLoading || appsQuery.isLoading;
  const error = query.error || appsQuery.error;

  const treePanel = (
    <div className="sm-list-tree-panel-inner">
      <Tree
        treeData={treeData}
        showLine={false}
        blockNode
        defaultExpandedKeys={[]}
        selectedKeys={selectedNodeKey ? [selectedNodeKey] : []}
        onSelect={handleTreeSelect}
      />
    </div>
  );

  return (
    <ListPage<MenuListVO>
      {...props}
      title="菜单管理"
      access={menuAccess}
      loading={loading ? true : false}
      error={error as Error | null}
      onRetry={() => {
        if (query.isError) query.refetch();
        if (appsQuery.isError) appsQuery.refetch();
      }}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索名称/路径"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      treePanel={treePanel}
      onAddNew={handleOpenAdd}
      onDelete={handleDelete}
      onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
      onDisable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })}
      enabledCommandLoading={enabledMutation.isPending}
      onRefresh={handleRefresh}
      onQuickSearch={onSearch}
      onPageChange={onPageChange}
      rowKey="id"
      columns={columns}
      dataSource={records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={(keys) => setSelectedRowKeys(keys)}
    />
  );
};

export default MenuListPage;
