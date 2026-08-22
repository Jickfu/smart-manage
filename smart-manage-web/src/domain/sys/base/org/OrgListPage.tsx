import { useMemo, useState } from 'react';
import { Button, Checkbox, Input, Tag } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import ListTree from '@/domain/common/page/ListTree';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { orgApi } from './api';
import OrgEditModal from './OrgEditModal';
import { orgAccess } from './permissions';
import { orgQueryKeys } from './queryKeys';
import type { OrgListVO, OrgTreeNode, OrgType } from './types';
import './OrgListPage.css';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' } },
  name: { label: '名称', filter: { type: 'string' } },
  namePath: { label: '长名称', filter: { type: 'string' } },
  orgType: {
    label: '组织类型',
    filter: {
      type: 'enum',
      options: [
        { label: '集团', value: 'GROUP' },
        { label: '公司', value: 'COMPANY' },
        { label: '部门', value: 'DEPARTMENT' },
      ],
    },
  },
  enabled: { label: '使用状态', filter: { type: 'boolean' } },
  archived: { label: '封存状态', filter: { type: 'boolean' } },
  archivedAt: { label: '封存日期', filter: { type: 'date' } },
  description: { label: '描述', filter: { type: 'string' } },
};

const ORG_TYPE_LABELS: Record<OrgType, string> = {
  GROUP: '集团',
  COMPANY: '公司',
  DEPARTMENT: '部门',
};

const toTreeNode = (node: OrgTreeNode): DataNode => ({
  key: node.id,
  title: node.archived ? `${node.name}（已封存）` : node.name,
  children: node.children.map(toTreeNode),
});

const filterTree = (nodes: OrgTreeNode[], keyword: string): OrgTreeNode[] => {
  const normalizedKeyword = keyword.trim().toLowerCase();
  if (!normalizedKeyword) return nodes;
  return nodes.flatMap((node) => {
    const children = filterTree(node.children, normalizedKeyword);
    const matches =
      node.name.toLowerCase().includes(normalizedKeyword) ||
      node.number.toLowerCase().includes(normalizedKeyword);
    return matches || children.length > 0 ? [{ ...node, children }] : [];
  });
};

const flattenTree = (nodes: OrgTreeNode[]): OrgTreeNode[] => {
  const result: OrgTreeNode[] = [];
  const visit = (items: OrgTreeNode[]) => {
    for (const item of items) {
      result.push(item);
      visit(item.children);
    }
  };
  visit(nodes);
  return result;
};

const OrgListPage = (props: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [selectedTreeKey, setSelectedTreeKey] = useState<string>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [treeKeyword, setTreeKeyword] = useState('');
  const [includeDescendants, setIncludeDescendants] = useState(false);
  const [showArchived, setShowArchived] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const treeQuery = useQuery({
    queryKey: orgQueryKeys.tree(showArchived),
    queryFn: () => orgApi.tree(showArchived),
  });
  const flatTreeNodes = useMemo(() => flattenTree(treeQuery.data ?? []), [treeQuery.data]);
  const effectiveTreeKey = flatTreeNodes.some((node) => node.id === selectedTreeKey)
    ? selectedTreeKey
    : treeQuery.data?.[0]?.id;
  const selectedTreeOrg = flatTreeNodes.find((node) => node.id === effectiveTreeKey);
  const listScope = {
    ...(effectiveTreeKey ? { parentId: effectiveTreeKey } : {}),
    includeDescendants,
    showArchived,
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
    columnQueryProps,
  } = useListPageQuery({
    queryKey: orgQueryKeys.list(listScope),
    queryFn: (params) => orgApi.listPage({ ...params, ...listScope }),
  });
  const selectedRecords = records.filter((record) => selectedRowKeys.includes(record.id));

  const refreshAll = async () => {
    setSelectedRowKeys([]);
    await queryClient.invalidateQueries({ queryKey: orgQueryKeys.all });
  };
  const enabledMutation = useEnabledMutation(orgApi.setEnabled, refreshAll);
  const archiveMutation = useCommandMutation({
    mutationFn: ({ ids, archived }: { ids: string[]; archived: boolean }) =>
      orgApi.setArchived(ids, archived),
    successMessage: (variables) => (variables.archived ? '组织封存成功' : '组织解封成功'),
    onSuccess: refreshAll,
  });

  const openEdit = (id: string | null) => {
    setEditingId(id);
    setModalOpen(true);
  };
  const columns: ColumnsType<OrgListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 160,
      render: (text, record) => (
        <Button
          type="link"
          size="small"
          disabled={record.archived}
          onClick={() => openEdit(record.id)}
        >
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: '长名称', dataIndex: 'namePath', ellipsis: true },
    {
      title: '组织类型',
      dataIndex: 'orgType',
      width: 100,
      render: (value: OrgType) => ORG_TYPE_LABELS[value],
    },
    {
      title: '使用状态',
      dataIndex: 'enabled',
      width: 100,
      render: (value: boolean) => (value ? <Tag color="green">启用</Tag> : <Tag>禁用</Tag>),
    },
    {
      title: '封存状态',
      dataIndex: 'archived',
      width: 100,
      render: (value: boolean) => (value ? <Tag color="orange">已封存</Tag> : <Tag>未封存</Tag>),
    },
    {
      title: '封存日期',
      dataIndex: 'archivedAt',
      width: 120,
      render: (value?: string) => value?.slice(0, 10) ?? '-',
    },
    { title: '描述', dataIndex: 'description', width: 220, ellipsis: true },
  ];

  const treePanel = (
    <ListTreePanel
      header={
        <Input.Search
          className="sm-org-tree-search"
          placeholder="搜索编码/名称"
          allowClear
          onChange={(event) => setTreeKeyword(event.target.value)}
        />
      }
      footer={
        <div className="sm-org-tree-options">
          <Checkbox
            checked={includeDescendants}
            onChange={(event) => {
              setIncludeDescendants(event.target.checked);
              setSelectedRowKeys([]);
            }}
          >
            包含下级
          </Checkbox>
          <Checkbox
            checked={showArchived}
            onChange={(event) => {
              setShowArchived(event.target.checked);
              setSelectedRowKeys([]);
            }}
          >
            显示封存
          </Checkbox>
        </div>
      }
    >
      <ListTree
        virtual={false}
        blockNode
        defaultExpandAll
        selectedKeys={effectiveTreeKey ? [effectiveTreeKey] : []}
        treeData={filterTree(treeQuery.data ?? [], treeKeyword).map(toTreeNode)}
        onSelect={(keys) => {
          setSelectedTreeKey(String(keys[0]));
          setSelectedRowKeys([]);
        }}
      />
    </ListTreePanel>
  );

  const confirmArchive = (archived: boolean) => {
    const actionLabel = archived ? '封存' : '解封';
    void confirmOperation({
      type: archived ? 'destructive' : 'normal',
      title: `确认${actionLabel}所选组织？`,
      description: archived
        ? `将处理 ${selectedRowKeys.length} 个组织。封存后组织会同时禁用，且不能继续编辑。`
        : `将处理 ${selectedRowKeys.length} 个组织。解封后组织仍保持禁用。`,
      confirmText: actionLabel,
      onConfirm: () => archiveMutation.mutateAsync({ ids: selectedRowKeys.map(String), archived }),
    });
  };

  return (
    <>
      <ListPage<OrgListVO>
        {...props}
        title="组织管理"
        access={orgAccess}
        treePanel={treePanel}
        loading={query.isLoading || treeQuery.isLoading}
        error={(query.error ?? treeQuery.error) as Error | null}
        onRetry={() => Promise.all([query.refetch(), treeQuery.refetch()])}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索编码/名称/长名称"
        filterSummary={keyword ? `关键字：${keyword}` : undefined}
        onAddNew={() => openEdit(null)}
        onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
        onDisable={() =>
          enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })
        }
        enabledCommandLoading={enabledMutation.isPending}
        toolbarActions={[
          {
            key: 'edit',
            label: '编辑',
            permission: orgAccess.permissions.save,
            disabled: selectedRecords.length !== 1 || selectedRecords[0]?.archived,
            onClick: () => {
              const record = selectedRecords[0];
              if (record) openEdit(record.id);
            },
          },
          {
            key: 'archive',
            label: '封存',
            permission: orgAccess.permissions.archive,
            danger: true,
            disabled:
              selectedRecords.length === 0 || selectedRecords.some((record) => record.archived),
            loading: archiveMutation.isPending,
            onClick: () => confirmArchive(true),
          },
          {
            key: 'unarchive',
            label: '解封',
            permission: orgAccess.permissions.unarchive,
            disabled:
              selectedRecords.length === 0 || selectedRecords.some((record) => !record.archived),
            loading: archiveMutation.isPending,
            onClick: () => confirmArchive(false),
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
      <OrgEditModal
        open={modalOpen}
        orgId={editingId}
        defaultParent={
          selectedTreeOrg?.archived
            ? undefined
            : selectedTreeOrg && {
                id: selectedTreeOrg.id,
                number: selectedTreeOrg.number,
                name: selectedTreeOrg.name,
              }
        }
        onClose={() => setModalOpen(false)}
        onSaved={async () => {
          setModalOpen(false);
          await refreshAll();
        }}
      />
    </>
  );
};

export default OrgListPage;
