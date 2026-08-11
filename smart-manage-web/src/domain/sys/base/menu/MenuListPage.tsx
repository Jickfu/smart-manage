import { useCallback, useMemo, useState } from 'react';
import { App, Button, Tag, Tree } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useMenuDeleteMutation } from './useMenuDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { menuApi } from './api';
import { menuQueryKeys } from './queryKeys';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import type { MenuTreeVO } from './types';
import { menuAccess } from './permissions';

/** 菜单编辑页 componentKey */
const MENU_EDIT_KEY = 'sys/base/menu/edit';
const ROOT_NODE_KEY = '__all__';

type MenuScope = { type: 'root' } | { type: 'cloud'; id: string } | { type: 'app'; id: string };

/** 树节点 key 格式：cloud:{cloudId} | app:{appId} */
function nodeKey(prefix: 'cloud' | 'app', id: string) {
  return `${prefix}:${id}`;
}

/** 菜单列表页 — 左侧按云/应用筛选，右侧按分组/页面展示完整层级。 */
const MenuListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const [selectedNodeKey, setSelectedNodeKey] = useState(ROOT_NODE_KEY);
  const [selectedScope, setSelectedScope] = useState<MenuScope>({ type: 'root' });
  const [keyword, setKeyword] = useState('');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);

  const appsQuery = useQuery({
    queryKey: appQueryKeys.cloudAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });

  const treeQuery = useQuery({
    queryKey: menuQueryKeys.treeList({
      type: selectedScope.type,
      id: selectedScope.type === 'root' ? '' : selectedScope.id,
      keyword,
    }),
    queryFn: () =>
      menuApi.listTree({
        cloudId: selectedScope.type === 'cloud' ? selectedScope.id : undefined,
        appId: selectedScope.type === 'app' ? selectedScope.id : undefined,
        keyword: keyword || undefined,
      }),
  });
  const records = useMemo(() => treeQuery.data ?? [], [treeQuery.data]);
  const pagedRecords = useMemo(
    () => records.slice((pageNum - 1) * pageSize, pageNum * pageSize),
    [pageNum, pageSize, records],
  );

  const deleteMutation = useMenuDeleteMutation(async () => {
    setSelectedRowKeys([]);
    setPageNum(1);
    await treeQuery.refetch();
  });
  const enabledMutation = useEnabledMutation(menuApi.setEnabled, async () => {
    setSelectedRowKeys([]);
    await treeQuery.refetch();
  });

  const handleRefresh = useCallback(async () => {
    // 菜单管理是左树右表聚合页面，手动刷新必须同步更新两侧数据。
    await Promise.all([appsQuery.refetch(), treeQuery.refetch()]);
  }, [appsQuery, treeQuery]);

  const treeData: DataNode[] = useMemo(
    () => [
      {
        key: ROOT_NODE_KEY,
        title: '全部',
        children:
          appsQuery.data?.map((cloud) => ({
            key: nodeKey('cloud', cloud.id),
            title: cloud.name,
            children: cloud.appList.map((app) => ({
              key: nodeKey('app', app.id),
              title: app.name,
              isLeaf: true,
            })),
          })) ?? [],
      },
    ],
    [appsQuery.data],
  );

  const handleTreeSelect = useCallback((keys: React.Key[]) => {
    const key = keys.length === 0 ? ROOT_NODE_KEY : String(keys[0]);
    setSelectedNodeKey(key);
    setSelectedRowKeys([]);
    setPageNum(1);
    if (key === ROOT_NODE_KEY) {
      setSelectedScope({ type: 'root' });
      return;
    }

    const separatorIndex = key.indexOf(':');
    const type = key.slice(0, separatorIndex);
    const id = key.slice(separatorIndex + 1);
    if (type === 'cloud') {
      setSelectedScope({ type: 'cloud', id });
    } else if (type === 'app') {
      setSelectedScope({ type: 'app', id });
    }
  }, []);

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, MENU_EDIT_KEY, id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, MENU_EDIT_KEY);
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

  const columns: ColumnsType<MenuTreeVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      ellipsis: true,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text || '-'}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', ellipsis: true },
    { title: '所属应用', dataIndex: 'appName', width: 100 },
    {
      title: '层级',
      dataIndex: 'level',
      width: 80,
      render: (value) =>
        value === 0 ? <Tag color="blue">分组</Tag> : <Tag color="green">页面</Tag>,
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

  const loading = treeQuery.isLoading || appsQuery.isLoading;
  const error = treeQuery.error || appsQuery.error;

  const treePanel = (
    <ListTreePanel>
      <Tree
        virtual={false}
        treeData={treeData}
        showLine={false}
        blockNode
        defaultExpandedKeys={[ROOT_NODE_KEY]}
        selectedKeys={[selectedNodeKey]}
        onSelect={handleTreeSelect}
      />
    </ListTreePanel>
  );

  return (
    <ListPage<MenuTreeVO>
      {...props}
      title="菜单"
      access={menuAccess}
      loading={loading}
      error={error as Error | null}
      onRetry={() => {
        if (treeQuery.isError) treeQuery.refetch();
        if (appsQuery.isError) appsQuery.refetch();
      }}
      total={records.length}
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
      onQuickSearch={(value) => {
        setSelectedRowKeys([]);
        setKeyword(value);
        setPageNum(1);
      }}
      onPageChange={(nextPageNum, nextPageSize) => {
        setPageNum(nextPageNum);
        setPageSize(nextPageSize);
      }}
      rowKey="id"
      columns={columns}
      dataSource={pagedRecords}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
      tableStateKey={`${treeQuery.dataUpdatedAt}:${pageNum}:${pageSize}`}
      expandable={{
        childrenColumnName: 'children',
        defaultExpandedRowKeys: pagedRecords.map((record) => record.id),
        rowExpandable: (record) => record.level === 0,
      }}
    />
  );
};

export default MenuListPage;
