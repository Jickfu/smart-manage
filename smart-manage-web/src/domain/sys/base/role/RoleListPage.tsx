import { useCallback } from 'react';
import { Button } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useListSelection } from '@/domain/common/page/useListSelection';
import { useRoleDeleteMutation } from './useRoleDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { OperationType } from '@/domain/common/page/types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import type { RoleListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { roleAccess } from './permissions';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

/** 角色编辑页 componentKey */
const ROLE_EDIT_KEY = componentKeys.roleEdit;
const ROLE_PERMISSION_ASSIGNMENT_KEY = componentKeys.rolePermissionAssignment;
const ROLE_DATA_SCOPE_ASSIGNMENT_KEY = componentKeys.roleDataScopeAssignment;

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  description: { label: '描述', filter: { type: 'string' } },
};

/** 角色管理列表页 */
const RoleListPage = (props: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
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
    queryKey: roleQueryKeys.lists(),
    queryFn: (params) => roleApi.listPage(params),
  });

  const { selectedRowKeys, setSelectedRowKeys, selectedIds, clearSelection } =
    useListSelection(records);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const addContentTab = useWorkbenchStore((state) => state.addContentTab);
  const deleteMutation = useRoleDeleteMutation(async () => {
    clearSelection();
    await query.refetch();
  });

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, ROLE_EDIT_KEY, id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, ROLE_EDIT_KEY);
  }, [props.appNumber, openAddNewTab]);

  const handleDelete = useCallback(() => {
    if (selectedRowKeys.length === 0) return;
    void confirmOperation({
      type: 'delete',
      title: '确认删除',
      description: `确定要删除选中的 ${selectedRowKeys.length} 条记录吗？`,
      confirmText: '删除',
      cancelText: '取消',
      onConfirm: () => deleteMutation.mutateAsync(selectedIds),
    });
  }, [selectedRowKeys, selectedIds, deleteMutation, confirmOperation]);

  const handleAssignPermissions = useCallback(() => {
    if (selectedRowKeys.length !== 1) return;
    const roleId = String(selectedRowKeys[0]);
    addContentTab(props.appNumber, {
      key: `assignment:${ROLE_PERMISSION_ASSIGNMENT_KEY}:${roleId}`,
      label: getRegisteredTabTitle(ROLE_PERMISSION_ASSIGNMENT_KEY, 'CUSTOM'),
      closable: true,
      componentKey: ROLE_PERMISSION_ASSIGNMENT_KEY,
      pageType: 'CUSTOM',
      billId: roleId,
    });
  }, [addContentTab, props.appNumber, selectedRowKeys]);

  const handleAssignDataScopes = useCallback(() => {
    if (selectedRowKeys.length !== 1) return;
    const roleId = String(selectedRowKeys[0]);
    addContentTab(props.appNumber, {
      key: `assignment:${ROLE_DATA_SCOPE_ASSIGNMENT_KEY}:${roleId}`,
      label: getRegisteredTabTitle(ROLE_DATA_SCOPE_ASSIGNMENT_KEY, 'CUSTOM'),
      closable: true,
      componentKey: ROLE_DATA_SCOPE_ASSIGNMENT_KEY,
      pageType: 'CUSTOM',
      billId: roleId,
    });
  }, [addContentTab, props.appNumber, selectedRowKeys]);

  const columns: ColumnsType<RoleListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 220 },
    { title: '描述', dataIndex: 'description', ellipsis: true },
  ];

  return (
    <ListPage<RoleListVO>
      {...props}
      title="角色"
      access={roleAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={handleOpenAdd}
      onDelete={handleDelete}
      onRefresh={onRefresh}
      toolbarActions={[
        {
          key: 'assignPermissions',
          label: '分配权限',
          permission: roleAccess.permissions.assignPermissions,
          disabled: selectedRowKeys.length !== 1,
          onClick: handleAssignPermissions,
        },
        {
          key: 'assignDataScopes',
          label: '分配数据范围',
          permission: roleAccess.permissions.assignDataScopes,
          disabled: selectedRowKeys.length !== 1,
          onClick: handleAssignDataScopes,
        },
      ]}
      onQuickSearch={onSearch}
      onPageChange={onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...columnQueryProps}
      dataSource={records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={(keys) => setSelectedRowKeys(keys)}
    />
  );
};

export default RoleListPage;
