import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useState } from 'react';
import { Button, Input, Tag } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import ListTree from '@/domain/common/page/list/ListTree';
import { PermissionActions } from '@/domain/common/page/access/PermissionActions';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useEnabledMutation } from '@/domain/common/page/command/useEnabledMutation';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { basicDataApi } from './api';
import BasicDataCategoryEditModal from './BasicDataCategoryEditModal';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataListVO, BasicDataTreeNode } from './types';
import './BasicDataListPage.css';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' } },
  name: { label: '名称', filter: { type: 'string' } },
  namePath: { label: '长名称', filter: { type: 'string' } },
  numberPath: { label: '长编码', filter: { type: 'string' } },
  level: { label: '级次', filter: { type: 'number' } },
  isLeaf: { label: '叶子节点', filter: { type: 'boolean' } },
  enabled: { label: '状态', filter: { type: 'boolean' } },
  systemPreset: { label: '系统预置', filter: { type: 'boolean' } },
  description: { label: '描述', filter: { type: 'string' } },
};

const EDIT_KEY = componentKeys.basicDataEdit;
const ROOT_KEY = 'basic-data-root';

const toTreeNode = (node: BasicDataTreeNode): DataNode => ({
  key: node.key,
  title: node.name,
  children: node.children.map(toTreeNode),
});

const filterTree = (nodes: BasicDataTreeNode[], keyword: string): BasicDataTreeNode[] => {
  if (!keyword.trim()) return nodes;
  const normalized = keyword.trim().toLowerCase();
  return nodes.flatMap((node) => {
    const children = filterTree(node.children, normalized);
    return node.name.toLowerCase().includes(normalized) || children.length > 0
      ? [{ ...node, children }]
      : [];
  });
};

const BasicDataListPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [selectedNode, setSelectedNode] = useState<BasicDataTreeNode>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [categoryModalOpen, setCategoryModalOpen] = useState(false);
  const [treeKeyword, setTreeKeyword] = useState('');
  const [editingCategoryId, setEditingCategoryId] = useState<string | null>(null);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);

  const treeQuery = useQuery({
    queryKey: basicDataQueryKeys.tree(),
    queryFn: basicDataApi.categoryTree,
  });
  const flatNodes = useMemo(
    () => treeQuery.data?.flatMap((domain) => [domain, ...domain.children]) ?? [],
    [treeQuery.data],
  );
  const selectedCategoryId = selectedNode?.type === 'category' ? selectedNode.id : undefined;
  const listCategoryId = selectedCategoryId ?? '-1';
  const selectedDomainId =
    selectedNode?.type === 'domain'
      ? selectedNode.id
      : treeQuery.data?.find((domain) =>
          domain.children.some((category) => category.id === selectedCategoryId),
        )?.id;

  const {
    records,
    total,
    pageNum,
    pageSize,
    query,
    onSearch,
    onPageChange,
    onRefresh,
    columnQueryProps,
  } = useListPageQuery({
    queryKey: basicDataQueryKeys.list({ categoryId: listCategoryId }),
    queryFn: (params) => basicDataApi.listPage({ ...params, categoryId: listCategoryId }),
  });
  const refreshAll = async () => {
    setSelectedRowKeys([]);
    await queryClient.invalidateQueries({ queryKey: basicDataQueryKeys.all });
  };
  const enabledMutation = useEnabledMutation(basicDataApi.setEnabled, refreshAll);
  const deleteMutation = useCommandMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      basicDataApi.delete(id, version),
    successMessage: '删除成功',
    onSuccess: refreshAll,
  });
  const deleteCategoryMutation = useCommandMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      basicDataApi.deleteCategory(id, version),
    successMessage: '分类删除成功',
    onSuccess: async () => {
      setSelectedNode(undefined);
      await refreshAll();
    },
  });

  const selectedRecords = records.filter((record) => selectedRowKeys.includes(record.id));
  const columns: ColumnsType<BasicDataListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 150,
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
    { title: '长名称', dataIndex: 'namePath', ellipsis: true },
    { title: '长编码', dataIndex: 'numberPath', width: 220, ellipsis: true },
    { title: '级次', dataIndex: 'level', width: 72 },
    {
      title: '叶子节点',
      dataIndex: 'isLeaf',
      width: 92,
      render: (value) => (value ? '是' : '否'),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (value) => (value ? <Tag color="green">启用</Tag> : <Tag>停用</Tag>),
    },
    {
      title: '系统预置',
      dataIndex: 'systemPreset',
      width: 92,
      render: (value) => (value ? '是' : '否'),
    },
    { title: '描述', dataIndex: 'description', width: 200, ellipsis: true },
  ];

  const treePanel = (
    <ListTreePanel
      header={
        <div className="sm-basic-data-tree-toolbar">
          <Input.Search
            placeholder="搜索名称"
            allowClear
            className="sm-basic-data-tree-search"
            onChange={(event) => setTreeKeyword(event.target.value)}
          />
          <div className="sm-basic-data-tree-actions">
            <PermissionActions
              prefix={basicDataAccess.prefix}
              actions={[
                {
                  key: 'add-category',
                  label: <PlusOutlined />,
                  permission: basicDataAccess.permissions.save,
                  disabled: !selectedDomainId,
                  onClick: () => {
                    setEditingCategoryId(null);
                    setCategoryModalOpen(true);
                  },
                },
                {
                  key: 'edit-category',
                  label: <EditOutlined />,
                  permission: basicDataAccess.permissions.save,
                  disabled: selectedNode?.type !== 'category',
                  onClick: () => {
                    setEditingCategoryId(selectedCategoryId ?? null);
                    setCategoryModalOpen(true);
                  },
                },
                {
                  key: 'delete-category',
                  label: <DeleteOutlined />,
                  permission: basicDataAccess.permissions.delete,
                  danger: true,
                  disabled: selectedNode?.type !== 'category',
                  onClick: async () => {
                    if (!selectedCategoryId) return;
                    const category = await basicDataApi.categoryDetail(selectedCategoryId);
                    void confirmOperation({
                      type: 'delete',
                      title: '确认删除基础资料分类？',
                      description: `${category.number} - ${category.name}`,
                      confirmText: '删除',
                      onConfirm: () =>
                        deleteCategoryMutation.mutateAsync({
                          id: category.id,
                          version: category.version,
                        }),
                    });
                  },
                },
              ]}
            />
          </div>
        </div>
      }
    >
      <ListTree
        virtual={false}
        blockNode
        defaultExpandedKeys={[ROOT_KEY]}
        selectedKeys={selectedNode ? [selectedNode.key] : []}
        treeData={[
          {
            key: ROOT_KEY,
            title: '基础资料',
            children: filterTree(treeQuery.data ?? [], treeKeyword).map(toTreeNode),
          },
        ]}
        onSelect={(keys) => {
          setSelectedRowKeys([]);
          setSelectedNode(flatNodes.find((node) => node.key === keys[0]));
        }}
      />
    </ListTreePanel>
  );

  return (
    <>
      <ListPage<BasicDataListVO>
        {...props}
        title="基础资料"
        access={basicDataAccess}
        treePanel={treePanel}
        loading={query.isLoading || treeQuery.isLoading}
        error={(query.error ?? treeQuery.error) as Error | null}
        onRetry={() => Promise.all([query.refetch(), treeQuery.refetch()])}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索编码/名称/长名称"
        onAddNew={() => {
          if (!selectedCategoryId) {
            void feedback.warning('请先选择基础资料分类');
            return;
          }
          openAddNewTab(props.appNumber, EDIT_KEY, {
            categoryId: selectedCategoryId,
          });
        }}
        onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
        onDisable={() =>
          enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })
        }
        enabledCommandLoading={enabledMutation.isPending}
        toolbarActions={[
          {
            key: 'delete',
            label: '删除',
            permission: basicDataAccess.permissions.delete,
            danger: true,
            disabled: selectedRecords.length !== 1,
            loading: deleteMutation.isPending,
            onClick: () => {
              const record = selectedRecords[0];
              if (!record) return;
              void confirmOperation({
                type: 'delete',
                title: '确认删除基础资料？',
                description: `${record.number} - ${record.name}`,
                confirmText: '删除',
                onConfirm: () =>
                  deleteMutation.mutateAsync({ id: record.id, version: record.version }),
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
      <BasicDataCategoryEditModal
        open={categoryModalOpen}
        categoryId={editingCategoryId}
        domainId={selectedDomainId}
        onClose={() => setCategoryModalOpen(false)}
        onSaved={async () => {
          setCategoryModalOpen(false);
          await refreshAll();
        }}
      />
    </>
  );
};

export default BasicDataListPage;
