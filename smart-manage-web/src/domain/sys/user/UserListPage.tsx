import { useState, useCallback } from 'react';
import { App, Button, Tag, Avatar } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useUserDeleteMutation } from './useUserDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { OperationType } from '@/domain/common/page/types';
import { userApi } from './api';
import { userQueryKeys } from './queryKeys';
import type { UserListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { userAccess } from './permissions';

/** 用户编辑页 componentKey */
const USER_EDIT_KEY = 'sys/base/user/edit';
const USER_ROLE_ASSIGNMENT_KEY = 'sys/base/user/role-assignment';

/** 用户管理列表页 */
const UserListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: userQueryKeys.lists(),
      queryFn: (params) => userApi.listPage(params),
    });

  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((s) => s.openBillTab);
  const openAddNewTab = useWorkbenchStore((s) => s.openAddNewTab);
  const addContentTab = useWorkbenchStore((state) => state.addContentTab);
  const deleteMutation = useUserDeleteMutation(async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });
  const enabledMutation = useEnabledMutation(userApi.setEnabled, async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, USER_EDIT_KEY, '编辑用户', id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, USER_EDIT_KEY, '新增用户');
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

  const handleAssignRoles = useCallback(() => {
    if (selectedRowKeys.length !== 1) return;
    const userId = String(selectedRowKeys[0]);
    addContentTab(props.appNumber, {
      key: `assignment:${USER_ROLE_ASSIGNMENT_KEY}:${userId}`,
      label: '分配角色',
      closable: true,
      componentKey: USER_ROLE_ASSIGNMENT_KEY,
      pageType: 'CUSTOM',
      billId: userId,
    });
  }, [addContentTab, props.appNumber, selectedRowKeys]);

  const columns: ColumnsType<UserListVO> = [
    {
      title: '用户名',
      dataIndex: 'username',
      width: 160,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text}
        </Button>
      ),
    },
    { title: '昵称', dataIndex: 'nickname' },
    {
      title: '头像',
      dataIndex: 'avatar',
      width: 60,
      render: (url) => (url ? <Avatar src={url} size="small" /> : <Avatar size="small">-</Avatar>),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (value) => (value ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>),
    },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];

  return (
    <ListPage<UserListVO>
      {...props}
      title="用户管理"
      access={userAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索用户名/昵称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={handleOpenAdd}
      onDelete={handleDelete}
      onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
      onDisable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })}
      enabledCommandLoading={enabledMutation.isPending}
      onRefresh={onRefresh}
      toolbarActions={[
        {
          key: 'assignRoles',
          label: '分配角色',
          permission: userAccess.permissions.assignRoles,
          disabled: selectedRowKeys.length !== 1,
          onClick: handleAssignRoles,
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

export default UserListPage;
