import { useState, useCallback } from 'react';
import { App, Avatar, Button, Input, Modal, Space, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useUserDeleteMutation } from './useUserDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { OperationType } from '@/domain/common/page/types';
import { userApi } from './api';
import { userQueryKeys } from './queryKeys';
import type { UserListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { userAccess } from './permissions';
import './UserListPage.css';

/** 用户编辑页 componentKey */
const USER_EDIT_KEY = 'sys/base/user/edit';
const USER_ROLE_ASSIGNMENT_KEY = 'sys/base/user/role-assignment';

/** 用户管理列表页 */
const UserListPage = (props: PageComponentProps) => {
  const { message, modal } = App.useApp();
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: userQueryKeys.lists(),
      queryFn: (params) => userApi.listPage(params),
    });

  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [resetPassword, setResetPassword] = useState<string>();
  const [resetUsername, setResetUsername] = useState('');
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
  const resetPasswordMutation = useCommandMutation({
    mutationFn: (id: string) => userApi.resetPassword(id),
    onSuccess: (result) => setResetPassword(result.password),
  });

  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, USER_EDIT_KEY, id, OperationType.EDIT);
    },
    [props.appNumber, openBillTab],
  );

  const handleOpenAdd = useCallback(() => {
    openAddNewTab(props.appNumber, USER_EDIT_KEY);
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
      label: getRegisteredTabTitle(USER_ROLE_ASSIGNMENT_KEY, 'CUSTOM'),
      closable: true,
      componentKey: USER_ROLE_ASSIGNMENT_KEY,
      pageType: 'CUSTOM',
      billId: userId,
    });
  }, [addContentTab, props.appNumber, selectedRowKeys]);

  const handleResetPassword = useCallback(() => {
    if (selectedRowKeys.length !== 1) return;
    const userId = String(selectedRowKeys[0]);
    const user = records.find((record) => record.id === userId);
    modal.confirm({
      title: '确认重置密码',
      content: `确定要重置用户“${user?.username ?? userId}”的密码吗？该用户已有登录状态将立即失效。`,
      okText: '重置',
      cancelText: '取消',
      onOk: async () => {
        setResetUsername(user?.username ?? userId);
        await resetPasswordMutation.mutateAsync(userId);
      },
    });
  }, [modal, records, resetPasswordMutation, selectedRowKeys]);

  const closeResetPasswordModal = useCallback(() => {
    setResetPassword(undefined);
    setResetUsername('');
  }, []);

  const copyResetPassword = useCallback(async () => {
    if (!resetPassword) return;
    try {
      await navigator.clipboard.writeText(resetPassword);
      message.success('新密码已复制');
    } catch {
      message.error('复制失败，请手动复制');
    }
  }, [message, resetPassword]);

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
    <>
      <ListPage<UserListVO>
        {...props}
        title="用户"
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
        onDisable={() =>
          enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })
        }
        enabledCommandLoading={enabledMutation.isPending}
        onRefresh={onRefresh}
        toolbarActions={[
          {
            key: 'resetPassword',
            label: '重置密码',
            permission: userAccess.permissions.resetPassword,
            disabled: selectedRowKeys.length !== 1,
            loading: resetPasswordMutation.isPending,
            onClick: handleResetPassword,
          },
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
      <Modal
        title="密码重置成功"
        open={Boolean(resetPassword)}
        closable
        mask={{ closable: false }}
        destroyOnHidden
        onCancel={closeResetPasswordModal}
        footer={[
          <Button key="close" onClick={closeResetPasswordModal}>
            关闭
          </Button>,
          <Button key="copy" type="primary" onClick={copyResetPassword}>
            复制密码
          </Button>,
        ]}
      >
        <Space orientation="vertical" className="sm-user-reset-password-content">
          <Typography.Text>
            用户“{resetUsername}”下次登录时必须修改密码。新密码仅展示一次，请及时复制。
          </Typography.Text>
          <Input value={resetPassword} readOnly />
        </Space>
      </Modal>
    </>
  );
};

export default UserListPage;
