import { useCallback, useMemo, useState } from 'react';
import { Button, Tag } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import ListTree from '@/domain/common/page/list/ListTree';
import { useEnabledMutation } from '@/domain/common/page/command/useEnabledMutation';
import { useMenuDeleteMutation } from './useMenuDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { menuApi } from './api';
import { menuQueryKeys } from './queryKeys';
import type { MenuCatalogNodeVO, MenuTreeVO } from './types';
import { menuAccess } from './permissions';
import {
  serializeListFilters,
  type ListColumnFeatures,
  type ListFilterCondition,
} from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' } },
  name: { label: '名称', filter: { type: 'string' } },
  level: {
    label: '层级',
    filter: {
      type: 'enum',
      options: [
        { label: '分组', value: 'CATEGORY' },
        { label: '页面', value: 'PAGE' },
      ],
    },
  },
  path: { label: '路径', filter: { type: 'string' } },
  component: { label: '组件', filter: { type: 'string' } },
  targetType: {
    label: '页面目标',
    filter: {
      type: 'enum',
      options: [
        { label: '内部页面', value: 'INTERNAL_PAGE' },
        { label: '外部链接', value: 'EXTERNAL_LINK' },
      ],
    },
  },
  externalUrl: { label: '外部链接', filter: { type: 'string' } },
  externalOpenMode: {
    label: '打开方式',
    filter: {
      type: 'enum',
      options: [
        { label: '新浏览器标签页', value: 'NEW_TAB' },
        { label: '工作台内嵌页', value: 'IFRAME' },
      ],
    },
  },
  sort: { label: '排序', filter: { type: 'number' } },
  enabled: { label: '状态', filter: { type: 'boolean' } },
};

/** 菜单编辑页 componentKey */
const MENU_EDIT_KEY = 'sys/base/menu/edit';
const ROOT_NODE_KEY = '__all__';

type MenuScope =
  | { type: 'root' }
  | { type: 'domain'; id: string }
  | { type: 'app'; id: string }
  | { type: 'feature'; id: string };

/** 树节点 key 格式：domain:{domainId} | app:{appId} | feature:{featureId}。 */
function nodeKey(prefix: 'domain' | 'app' | 'feature', id: string) {
  return `${prefix}:${id}`;
}

function toTreeNode(node: MenuCatalogNodeVO): DataNode {
  const prefix = node.type === 'APPLICATION' ? 'app' : node.type.toLowerCase();
  return {
    key: nodeKey(prefix as 'domain' | 'app' | 'feature', node.id),
    title: node.name,
    isLeaf: node.type === 'FEATURE',
    children: node.children.map(toTreeNode),
  };
}

/** 菜单列表页 — 左侧按领域/应用/功能筛选，右侧按分组/页面展示完整层级。 */
const MenuListPage = (props: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const [selectedNodeKey, setSelectedNodeKey] = useState(ROOT_NODE_KEY);
  const [selectedScope, setSelectedScope] = useState<MenuScope>({ type: 'root' });
  const [keyword, setKeyword] = useState('');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [columnFilters, setColumnFilters] = useState<ListFilterCondition[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);

  const catalogQuery = useQuery({
    queryKey: menuQueryKeys.catalog(),
    queryFn: menuApi.catalog,
    staleTime: 5 * 60 * 1000,
  });

  const treeQuery = useQuery({
    queryKey: menuQueryKeys.treeList({
      type: selectedScope.type,
      id: selectedScope.type === 'root' ? '' : selectedScope.id,
      keyword,
      filters: serializeListFilters(columnFilters),
    }),
    queryFn: () =>
      menuApi.listTree({
        domainId: selectedScope.type === 'domain' ? selectedScope.id : undefined,
        appId: selectedScope.type === 'app' ? selectedScope.id : undefined,
        featureId: selectedScope.type === 'feature' ? selectedScope.id : undefined,
        keyword: keyword || undefined,
        filters: serializeListFilters(columnFilters),
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
    await Promise.all([catalogQuery.refetch(), treeQuery.refetch()]);
  }, [catalogQuery, treeQuery]);

  const treeData: DataNode[] = useMemo(
    () => [
      {
        key: ROOT_NODE_KEY,
        title: '全部菜单',
        children: catalogQuery.data?.map(toTreeNode) ?? [],
      },
    ],
    [catalogQuery.data],
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
    if (type === 'domain') {
      setSelectedScope({ type: 'domain', id });
    } else if (type === 'app') {
      setSelectedScope({ type: 'app', id });
    } else if (type === 'feature') {
      setSelectedScope({ type: 'feature', id });
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
    void confirmOperation({
      type: 'delete',
      title: '确认删除',
      description: `确定要删除选中的 ${selectedRowKeys.length} 条记录吗？`,
      confirmText: '删除',
      cancelText: '取消',
      onConfirm: () => deleteMutation.mutateAsync(selectedRowKeys.map(String)),
    });
  }, [selectedRowKeys, deleteMutation, confirmOperation]);

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
    {
      title: '页面目标',
      dataIndex: 'targetType',
      width: 100,
      render: (value) =>
        value === 'EXTERNAL_LINK' ? (
          <Tag color="orange">外部链接</Tag>
        ) : value === 'INTERNAL_PAGE' ? (
          <Tag color="blue">内部页面</Tag>
        ) : (
          '-'
        ),
    },
    { title: '外部链接', dataIndex: 'externalUrl', width: 220, ellipsis: true },
    {
      title: '打开方式',
      dataIndex: 'externalOpenMode',
      width: 130,
      render: (value) =>
        value === 'NEW_TAB' ? '新浏览器标签页' : value === 'IFRAME' ? '工作台内嵌页' : '-',
    },
    { title: '组件', dataIndex: 'component', ellipsis: true },
    { title: '排序', dataIndex: 'sort', width: 60 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (value) => (value ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
    },
  ];

  const loading = treeQuery.isLoading || catalogQuery.isLoading;
  const error = treeQuery.error || catalogQuery.error;

  const treePanel = (
    <ListTreePanel>
      <ListTree
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
        if (catalogQuery.isError) catalogQuery.refetch();
      }}
      total={records.length}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索名称/路径/外部链接"
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
      columnFeatures={columnFeatures}
      columnFilters={columnFilters}
      onColumnFiltersChange={(filters) => {
        setColumnFilters(filters);
        setSelectedRowKeys([]);
        setPageNum(1);
      }}
      dataSource={pagedRecords}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
      rowClassName={(record) =>
        record.level === 0 ? 'sm-list-row-tone-alternate' : 'sm-list-row-tone-base'
      }
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
