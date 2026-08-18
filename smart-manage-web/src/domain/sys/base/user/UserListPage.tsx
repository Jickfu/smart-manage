import { useCallback, useMemo, useState } from 'react';
import { App, Button, Checkbox, Form, Input, Modal, Space, Tag, Typography } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import { sm2 } from 'sm-crypto';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ListPage from '@/domain/common/page/ListPage';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import ListTree from '@/domain/common/page/ListTree';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useUserDeleteMutation } from './useUserDeleteMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { getRegisteredTabTitle } from '@/domain/common/registry/componentRegistry';
import { OperationType } from '@/domain/common/page/types';
import { orgApi } from '@/domain/sys/base/org/api';
import { orgQueryKeys } from '@/domain/sys/base/org/queryKeys';
import type { OrgTreeNode } from '@/domain/sys/base/org/types';
import { userApi } from './api';
import { userQueryKeys } from './queryKeys';
import type { UserAssignmentVO, UserListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { userAccess } from './permissions';
import { UserAvatar } from './UserAvatar';
import AppModal from '@/domain/common/component/AppModal';
import './UserListPage.css';
import type { ListColumnFeatures } from '@/domain/common/page/listQuery';

const columnFeatures: ListColumnFeatures = {
  name: { label: '姓名', filter: { type: 'string' }, sorter: true },
  number: { label: '工号', filter: { type: 'string' }, sorter: true },
  username: { label: '用户名', filter: { type: 'string' }, sorter: true },
  enabled: { label: '账号状态', filter: { type: 'boolean' }, sorter: true },
};

const USER_EDIT_KEY = 'sys/base/user/edit';
const USER_ROLE_ASSIGNMENT_KEY = 'sys/base/user/role-assignment';
const UNASSIGNED_KEY = '__unassigned__';

interface TemporaryLoginFormValues {
  reason: string;
  administratorPassword?: string;
}

const toTreeNode = (node: OrgTreeNode): DataNode => ({
  key: node.id,
  title: node.name,
  children: node.children.map(toTreeNode),
});

const filterTree = (nodes: OrgTreeNode[], keyword: string): OrgTreeNode[] => {
  const normalized = keyword.trim().toLowerCase();
  if (!normalized) return nodes;
  return nodes.flatMap((node) => {
    const children = filterTree(node.children, normalized);
    const matches =
      node.name.toLowerCase().includes(normalized) ||
      node.number.toLowerCase().includes(normalized);
    return matches || children.length ? [{ ...node, children }] : [];
  });
};

const AssignmentCells = ({
  assignments,
  render,
}: {
  assignments: UserAssignmentVO[];
  render: (assignment: UserAssignmentVO) => React.ReactNode;
}) => (
  <div className="sm-user-assignment-cells">
    {assignments.map((assignment) => (
      <div key={assignment.id ?? assignment.orgId} className="sm-user-assignment-cell">
        {render(assignment)}
      </div>
    ))}
  </div>
);

const UserListPage = (props: PageComponentProps) => {
  const { message, modal } = App.useApp();
  const [selectedTreeKey, setSelectedTreeKey] = useState<string>();
  const [treeKeyword, setTreeKeyword] = useState('');
  const [includeDescendants, setIncludeDescendants] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [resetPassword, setResetPassword] = useState<string>();
  const [resetUsername, setResetUsername] = useState('');
  const [temporaryLoginOpen, setTemporaryLoginOpen] = useState(false);
  const [temporaryLoginSafe, setTemporaryLoginSafe] = useState(false);
  const [temporaryLoginUser, setTemporaryLoginUser] = useState<UserListVO>();
  const [temporaryLoginCredential, setTemporaryLoginCredential] = useState<string>();
  const [temporaryLoginExpiresAt, setTemporaryLoginExpiresAt] = useState('');
  const [temporaryLoginForm] = Form.useForm<TemporaryLoginFormValues>();
  const treeQuery = useQuery({
    queryKey: orgQueryKeys.tree(false),
    queryFn: () => orgApi.tree(false),
  });
  const effectiveTreeKey = selectedTreeKey ?? treeQuery.data?.[0]?.id;
  const unassigned = effectiveTreeKey === UNASSIGNED_KEY;
  const scope = {
    ...(unassigned ? { unassigned: true } : effectiveTreeKey ? { orgId: effectiveTreeKey } : {}),
    includeDescendants: unassigned ? false : includeDescendants,
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
    queryKey: userQueryKeys.list(scope),
    queryFn: (params) => userApi.listPage({ ...params, ...scope }),
  });
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
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
  const temporaryLoginMutation = useCommandMutation({
    mutationFn: async (values: TemporaryLoginFormValues) => {
      if (!temporaryLoginUser) throw new Error('未选择目标用户');
      if (!temporaryLoginSafe) {
        const publicKey = await userApi.temporaryLoginPublicKey();
        const encryptedPassword = sm2.doEncrypt(values.administratorPassword ?? '', publicKey, 1);
        await userApi.openTemporaryLoginSafe(encryptedPassword);
      }
      return userApi.createTemporaryLoginGrant(temporaryLoginUser.id, values.reason);
    },
    onSuccess: (result) => {
      setTemporaryLoginOpen(false);
      setTemporaryLoginCredential(result.credential);
      setTemporaryLoginExpiresAt(result.expiresAt);
      temporaryLoginForm.resetFields();
    },
  });
  const handleOpenEdit = useCallback(
    (id: string) => {
      openBillTab(props.appNumber, USER_EDIT_KEY, id, OperationType.EDIT);
    },
    [openBillTab, props.appNumber],
  );

  const columns = useMemo<ColumnsType<UserListVO>>(
    () => [
      {
        key: 'avatar',
        title: '头像',
        dataIndex: 'avatar',
        width: 58,
        render: (src, record) => (
          <UserAvatar
            src={src}
            name={record.name}
            number={record.number}
            username={record.username}
          />
        ),
      },
      {
        key: 'name',
        title: '姓名',
        dataIndex: 'name',
        width: 120,
        render: (text, record) => (
          <button type="button" className="sm-table-link" onClick={() => handleOpenEdit(record.id)}>
            {text}
          </button>
        ),
      },
      { key: 'number', title: '工号', dataIndex: 'number', width: 120 },
      {
        key: 'username',
        title: '用户名',
        dataIndex: 'username',
        width: 140,
      },
      {
        title: '部门',
        key: 'orgName',
        width: 150,
        render: (_, record) => (
          <AssignmentCells
            assignments={record.assignments}
            render={(assignment) => assignment.orgName}
          />
        ),
      },
      {
        title: '部门长名称',
        key: 'orgNamePath',
        render: (_, record) => (
          <AssignmentCells
            assignments={record.assignments}
            render={(assignment) => assignment.orgNamePath}
          />
        ),
      },
      {
        title: '职位',
        key: 'position',
        width: 140,
        render: (_, record) => (
          <AssignmentCells
            assignments={record.assignments}
            render={(assignment) => assignment.position}
          />
        ),
      },
      {
        title: '负责人',
        key: 'isOrgLeader',
        width: 76,
        align: 'center',
        render: (_, record) => (
          <AssignmentCells
            assignments={record.assignments}
            render={(assignment) =>
              assignment.isOrgLeader ? (
                <CheckOutlined className="sm-user-assignment-check" title="部门负责人" />
              ) : null
            }
          />
        ),
      },
      {
        title: '主职',
        key: 'isPrimary',
        width: 64,
        align: 'center',
        render: (_, record) => (
          <AssignmentCells
            assignments={record.assignments}
            render={(assignment) =>
              assignment.isPrimary ? (
                <CheckOutlined className="sm-user-assignment-check" title="主职" />
              ) : null
            }
          />
        ),
      },
      {
        key: 'enabled',
        title: '账号状态',
        dataIndex: 'enabled',
        width: 90,
        render: (value) => (value ? <Tag color="green">可用</Tag> : <Tag>禁用</Tag>),
      },
    ],
    [handleOpenEdit],
  );

  const treePanel = (
    <ListTreePanel
      header={
        <Input.Search
          placeholder="搜索组织"
          allowClear
          onChange={(event) => setTreeKeyword(event.target.value)}
        />
      }
      footer={
        <Checkbox
          disabled={unassigned}
          checked={!unassigned && includeDescendants}
          onChange={(event) => {
            setIncludeDescendants(event.target.checked);
            setSelectedRowKeys([]);
          }}
        >
          包含下级
        </Checkbox>
      }
    >
      <ListTree
        virtual={false}
        blockNode
        defaultExpandAll
        selectedKeys={effectiveTreeKey ? [effectiveTreeKey] : []}
        treeData={[
          ...filterTree(treeQuery.data ?? [], treeKeyword).map(toTreeNode),
          ...('未分配部门'.includes(treeKeyword.trim())
            ? [{ key: UNASSIGNED_KEY, title: '未分配部门' }]
            : []),
        ]}
        onSelect={(keys) => {
          setSelectedTreeKey(String(keys[0]));
          setSelectedRowKeys([]);
        }}
      />
    </ListTreePanel>
  );

  const selectedUser = records.find((record) => selectedRowKeys[0] === record.id);
  return (
    <>
      <ListPage<UserListVO>
        {...props}
        title="用户"
        access={userAccess}
        treePanel={treePanel}
        columnSettingsKey={props.componentKey}
        loading={query.isLoading || treeQuery.isLoading}
        error={(query.error ?? treeQuery.error) as Error | null}
        onRetry={() => Promise.all([query.refetch(), treeQuery.refetch()])}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索姓名/工号/用户名"
        filterSummary={keyword ? `关键字：${keyword}` : undefined}
        onAddNew={() => openAddNewTab(props.appNumber, USER_EDIT_KEY)}
        onDelete={() =>
          modal.confirm({
            title: '确认删除',
            content: `确定删除选中的 ${selectedRowKeys.length} 个用户吗？`,
            okType: 'danger',
            onOk: () => deleteMutation.mutateAsync(selectedRowKeys.map(String)),
          })
        }
        onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
        onDisable={() =>
          enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })
        }
        enabledCommandLoading={enabledMutation.isPending}
        toolbarActions={[
          {
            key: 'resetPassword',
            label: '重置密码',
            permission: userAccess.permissions.resetPassword,
            disabled: selectedRowKeys.length !== 1,
            loading: resetPasswordMutation.isPending,
            onClick: () => {
              if (!selectedUser) return;
              modal.confirm({
                title: '确认重置密码',
                content: `确定重置用户“${selectedUser.username}”的密码吗？`,
                onOk: async () => {
                  setResetUsername(selectedUser.username);
                  await resetPasswordMutation.mutateAsync(selectedUser.id);
                },
              });
            },
          },
          {
            key: 'temporaryLogin',
            label: '生成代登录密码',
            permission: userAccess.permissions.temporaryLogin,
            disabled: selectedRowKeys.length !== 1 || selectedUser?.username === 'administrator',
            loading: temporaryLoginMutation.isPending,
            onClick: async () => {
              if (!selectedUser) return;
              const safe = await userApi.temporaryLoginSafe();
              setTemporaryLoginSafe(safe);
              setTemporaryLoginUser(selectedUser);
              setTemporaryLoginOpen(true);
            },
          },
          {
            key: 'assignRoles',
            label: '分配角色',
            permission: userAccess.permissions.assignRoles,
            disabled: selectedRowKeys.length !== 1,
            onClick: () => {
              if (!selectedUser) return;
              addContentTab(props.appNumber, {
                key: `assignment:${USER_ROLE_ASSIGNMENT_KEY}:${selectedUser.id}`,
                label: getRegisteredTabTitle(USER_ROLE_ASSIGNMENT_KEY, 'CUSTOM'),
                closable: true,
                componentKey: USER_ROLE_ASSIGNMENT_KEY,
                pageType: 'CUSTOM',
                billId: selectedUser.id,
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
        striped
        selectMode="checkbox"
        selectedRowKeys={selectedRowKeys}
        onSelectChange={setSelectedRowKeys}
      />
      <Modal
        title="密码重置成功"
        open={Boolean(resetPassword)}
        mask={{ closable: false }}
        onCancel={() => setResetPassword(undefined)}
        footer={
          <Button
            type="primary"
            onClick={async () => {
              if (resetPassword) {
                await navigator.clipboard.writeText(resetPassword);
                message.success('新密码已复制');
              }
            }}
          >
            复制密码
          </Button>
        }
      >
        <Space orientation="vertical" className="sm-user-reset-password-content">
          <Typography.Text>用户“{resetUsername}”下次登录时必须修改密码。</Typography.Text>
          <Input value={resetPassword} readOnly />
        </Space>
      </Modal>
      <AppModal
        title={`生成“${temporaryLoginUser?.username ?? ''}”的代登录密码`}
        open={temporaryLoginOpen}
        width={520}
        bodyMode="natural"
        closeDisabled={temporaryLoginMutation.isPending}
        onCancel={() => {
          setTemporaryLoginOpen(false);
          temporaryLoginForm.resetFields();
        }}
        footer={
          <>
            <Button
              onClick={() => {
                setTemporaryLoginOpen(false);
                temporaryLoginForm.resetFields();
              }}
              disabled={temporaryLoginMutation.isPending}
            >
              取消
            </Button>
            <Button
              type="primary"
              loading={temporaryLoginMutation.isPending}
              onClick={() => temporaryLoginForm.submit()}
            >
              生成
            </Button>
          </>
        }
      >
        <Form
          form={temporaryLoginForm}
          layout="vertical"
          className="sm-user-temporary-login-form"
          onFinish={(values) => temporaryLoginMutation.mutate(values)}
        >
          <Form.Item
            name="reason"
            label="代登录原因"
            rules={[
              { required: true, whitespace: true, message: '请输入代登录原因' },
              { max: 500, message: '代登录原因不能超过500个字符' },
            ]}
          >
            <Input.TextArea rows={3} maxLength={500} showCount />
          </Form.Item>
          {!temporaryLoginSafe && (
            <Form.Item
              name="administratorPassword"
              label="administrator 密码"
              extra="验证通过后5分钟内再次生成无需重复输入。"
              rules={[{ required: true, message: '请输入 administrator 密码' }]}
            >
              <Input.Password autoComplete="current-password" />
            </Form.Item>
          )}
        </Form>
      </AppModal>
      <Modal
        title="代登录密码生成成功"
        open={Boolean(temporaryLoginCredential)}
        mask={{ closable: false }}
        onCancel={() => setTemporaryLoginCredential(undefined)}
        footer={
          <Button
            type="primary"
            onClick={async () => {
              if (temporaryLoginCredential) {
                await navigator.clipboard.writeText(temporaryLoginCredential);
                message.success('代登录密码已复制');
              }
            }}
          >
            复制代登录密码
          </Button>
        }
      >
        <Space orientation="vertical" className="sm-user-reset-password-content">
          <Typography.Text>
            请在 {temporaryLoginExpiresAt}{' '}
            前，于无痕窗口使用目标用户名和下方密码登录。密码仅展示一次且成功登录后立即失效。
          </Typography.Text>
          <Input value={temporaryLoginCredential} readOnly />
        </Space>
      </Modal>
    </>
  );
};

export default UserListPage;
