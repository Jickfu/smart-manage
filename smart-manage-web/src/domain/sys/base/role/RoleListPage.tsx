import { formatOperationTargets } from '@/domain/common/component/formatOperationTargets';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useCallback } from 'react';
import { Button } from 'antd';
import CheckOutlined from '@ant-design/icons/CheckOutlined';
import './RoleListPage.css';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { userQueryKeys } from '@/domain/sys/base/user/queryKeys';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useListSelection } from '@/domain/common/page/list/useListSelection';
import { useRoleDeleteMutation } from './useRoleDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { OperationType } from '@/domain/common/page/types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import type { RoleListVO, RoleDefaultSaveForm } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { roleAccess } from './permissions';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';

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
  const queryClient = useQueryClient();
  const defaultQuery = useQuery({
    queryKey: roleQueryKeys.defaultRole(),
    queryFn: roleApi.defaultRole,
    meta: { errorPresentation: 'local-initial' },
  });
  const defaultMutation = useCommandMutation({
    mutationFn: (form: RoleDefaultSaveForm) => roleApi.setDefault(form),
    successMessage: '默认角色设置成功',
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: roleQueryKeys.all }),
        queryClient.invalidateQueries({ queryKey: userQueryKeys.all }),
        queryClient.invalidateQueries({ queryKey: ['current-user-permissions'] }),
      ]);
    },
  });
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
    const commandIds = [...selectedIds];
    const targets = commandIds.map((id) => {
      const record = records.find((item) => item.id === id);
      return record ? `${record.number} — ${record.name}` : `ID ${id}`;
    });
    void confirmOperation({
      type: 'delete',
      title: '确认删除',
      description: `将删除以下角色，是否继续？\n${formatOperationTargets(targets)}`,
      confirmText: '删除',
      cancelText: '取消',
      onConfirm: () => deleteMutation.mutateAsync(commandIds),
    });
  }, [selectedRowKeys, selectedIds, records, deleteMutation, confirmOperation]);

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

  const selectedRole =
    selectedIds.length === 1 ? records.find((record) => record.id === selectedIds[0]) : undefined;
  const selectedIsDefault = Boolean(
    selectedRole && defaultQuery.data?.role?.id === selectedRole.id,
  );
  const handleSetDefault = () => {
    const configuration = defaultQuery.data;
    if (!configuration || !selectedRole) return;
    // 文案与命令使用同一份配置快照，确认期间的并发变化交给服务端版本校验。
    const command = {
      roleId: selectedIsDefault ? null : selectedRole.id,
      previousRoleId: configuration.role?.id ?? null,
      version: configuration.version,
    };
    const previous = configuration.role
      ? `“${configuration.role.number} — ${configuration.role.name}”`
      : '未设置';
    const target = selectedIsDefault ? '未设置' : `“${selectedRole.number} — ${selectedRole.name}”`;
    void confirmOperation({
      type: 'warning',
      title: selectedIsDefault ? '取消默认角色' : '设置默认角色',
      description: `默认角色将从${previous}变更为${target}。所有普通用户的基础权限和数据范围将随之变化；手工分配的角色保留。`,
      confirmText: '确认',
      onConfirm: () => defaultMutation.mutateAsync(command),
    });
  };

  const columns: ColumnsType<RoleListVO> = [
    {
      ellipsis: true,
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text}
        </Button>
      ),
    },
    { ellipsis: true, title: '名称', dataIndex: 'name', width: 220 },
    {
      ellipsis: true,
      title: '默认角色',
      key: 'defaultRole',
      width: 76,
      align: 'center',
      render: (_value, record) =>
        defaultQuery.data?.role?.id === record.id ? (
          <CheckOutlined className="sm-role-default-check" title="默认角色" />
        ) : null,
    },
    { title: '描述', dataIndex: 'description', ellipsis: true },
  ];

  return (
    <ListPage<RoleListVO>
      {...props}
      title="角色"
      access={roleAccess}
      loading={query.isLoading || defaultQuery.isLoading}
      error={(getBlockingQueryError(query) ?? getBlockingQueryError(defaultQuery)) as Error | null}
      onRetry={() => Promise.all([query.refetch(), defaultQuery.refetch()])}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={handleOpenAdd}
      onDelete={handleDelete}
      onRefresh={() => {
        onRefresh();
        void defaultQuery.refetch();
      }}
      toolbarActions={[
        { builtin: 'add' },
        { builtin: 'refresh' },
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
        {
          key: 'setDefault',
          label: selectedIsDefault ? '取消默认角色' : '设为默认角色',
          permission: roleAccess.permissions.setDefault,
          disabled:
            !selectedRole ||
            !defaultQuery.data ||
            defaultQuery.isFetching ||
            defaultMutation.isPending,
          onClick: handleSetDefault,
        },
        { builtin: 'delete' },
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
