import { useState, useCallback } from 'react';
import { App, Button } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useRoleDeleteMutation } from './useRoleDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { OperationType } from '@/domain/common/page/types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import type { RoleListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { roleAccess } from './permissions';

/** 角色编辑页 componentKey */
const ROLE_EDIT_KEY = 'sys/base/role/edit';
const ROLE_PERMISSION_ASSIGNMENT_KEY = 'sys/base/role/permission-assignment';

/** 角色管理列表页 */
const RoleListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: roleQueryKeys.lists(),
      queryFn: (params) => roleApi.listPage(params),
    });

  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((s) => s.openBillTab);
  const openAddNewTab = useWorkbenchStore((s) => s.openAddNewTab);
  const addContentTab = useWorkbenchStore((state) => state.addContentTab);
  const deleteMutation = useRoleDeleteMutation(async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, ROLE_EDIT_KEY, '编辑角色', id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, ROLE_EDIT_KEY, '新增角色');
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

  const handleAssignPermissions = useCallback(() => {
    if (selectedRowKeys.length !== 1) return;
    const roleId = String(selectedRowKeys[0]);
    addContentTab(props.appNumber, {
      key: `assignment:${ROLE_PERMISSION_ASSIGNMENT_KEY}:${roleId}`,
      label: '分配权限',
      closable: true,
      componentKey: ROLE_PERMISSION_ASSIGNMENT_KEY,
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
    { title: '备注', dataIndex: 'remark', ellipsis: true },
  ];

  return (
    <ListPage<RoleListVO>
      {...props}
      title="角色管理"
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
      ]}
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

export default RoleListPage;
