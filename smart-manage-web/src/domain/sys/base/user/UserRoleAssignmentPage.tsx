import { useMemo, useState } from 'react';
import { Button, Descriptions, Input, Splitter, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import RefSelector from '@/domain/common/component/RefSelector';
import { AssignmentPage } from '@/domain/common/page/assignment/AssignmentPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import type { RoleSelectVO } from '@/domain/sys/base/role/types';
import { useRoleRefSelector } from '@/domain/sys/base/role/refSelector/useRoleRefSelector';
import type { PageComponentProps } from '@/domain/common/page/types';
import { userApi } from './api';
import { userAccess } from './permissions';
import { userQueryKeys } from './queryKeys';
import type { UserRoleOrganizationVO } from './types';
import './UserRoleAssignmentPage.css';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';

type RolesByOrgId = Record<string, RoleSelectVO[]>;

const roleColumns: ColumnsType<RoleSelectVO> = [
  {
    title: '#',
    width: 44,
    align: 'center',
    className: 'sm-assignment-sequence-column',
    render: (_value, _record, index) => index + 1,
  },
  { title: '编码', dataIndex: 'number', width: 160 },
  { title: '名称', dataIndex: 'name', width: 180 },
  { title: '描述', dataIndex: 'description', render: (description) => description || '—' },
];

const buildRolesByOrgId = (organizations: UserRoleOrganizationVO[]): RolesByOrgId =>
  Object.fromEntries(
    organizations.map((organization) => [organization.org.id, organization.roles]),
  );

const canonicalize = (assignments: RolesByOrgId) =>
  Object.entries(assignments)
    .sort(([leftOrgId], [rightOrgId]) => leftOrgId.localeCompare(rightOrgId))
    .map(([orgId, roles]) => [orgId, roles.map((role) => role.id).sort()] as const);

/** 用户角色分配专用页面。 */
const UserRoleAssignmentPage = ({ appNumber, tabKey, billId, context }: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [localAssignments, setLocalAssignments] = useState<RolesByOrgId | null>(null);
  const [selectedOrgId, setSelectedOrgId] = useState<string | undefined>(context?.orgId);
  const [roleKeyword, setRoleKeyword] = useState('');
  const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([]);

  const workspaceQuery = useQuery({
    queryKey: userQueryKeys.roleAssignmentWorkspace(billId),
    queryFn: () => userApi.roleAssignmentWorkspace(billId!),
    enabled: Boolean(billId),
  });
  const organizations = useMemo(
    () => workspaceQuery.data?.organizations ?? [],
    [workspaceQuery.data?.organizations],
  );
  const initialAssignments = useMemo(() => buildRolesByOrgId(organizations), [organizations]);
  const assignments = localAssignments ?? initialAssignments;
  const effectiveOrgId =
    (selectedOrgId && organizations.some((item) => item.org.id === selectedOrgId)
      ? selectedOrgId
      : undefined) ??
    organizations.find((item) => item.isPrimary)?.org.id ??
    organizations[0]?.org.id;
  const selectedOrganization = organizations.find((item) => item.org.id === effectiveOrgId);
  const currentRoles = effectiveOrgId ? (assignments[effectiveOrgId] ?? []) : [];
  const currentRoleIds = currentRoles.map((role) => role.id);
  const normalizedRoleKeyword = roleKeyword.trim().toLowerCase();
  const visibleAssignedRoles = currentRoles.filter((role) =>
    !normalizedRoleKeyword
      ? true
      : [role.number, role.name, role.description]
          .filter(Boolean)
          .some((value) => value!.toLowerCase().includes(normalizedRoleKeyword)),
  );
  const roleRefSelector = useRoleRefSelector({
    orgId: effectiveOrgId,
    orgName: selectedOrganization?.org.name,
    excludedIds: currentRoleIds,
  });
  const dirty =
    JSON.stringify(canonicalize(assignments)) !== JSON.stringify(canonicalize(initialAssignments));

  const mutation = useCommandMutation({
    mutationFn: () =>
      userApi.saveRoleAssignment({
        userId: billId!,
        assignments: organizations.map((organization) => ({
          orgId: organization.org.id,
          roleIds: (assignments[organization.org.id] ?? []).map((role) => role.id),
        })),
      }),
    successMessage: '角色分配成功',
    onSuccess: async () => {
      setLocalAssignments(null);
      setSelectedRoleIds([]);
      await queryClient.invalidateQueries({
        queryKey: userQueryKeys.roleAssignmentWorkspace(billId),
      });
    },
  });

  const updateCurrentRoles = (nextRoles: RoleSelectVO[]) => {
    if (!effectiveOrgId) return;
    setLocalAssignments({ ...assignments, [effectiveOrgId]: nextRoles });
    setSelectedRoleIds([]);
  };

  const addSelectedRoles = (value: Record<string, unknown> | Record<string, unknown>[] | null) => {
    if (!Array.isArray(value)) return;
    updateCurrentRoles([...currentRoles, ...(value as unknown as RoleSelectVO[])]);
  };

  const deleteSelectedRoles = () => {
    if (selectedRoleIds.length === 0 || !selectedOrganization) return;
    void confirmOperation({
      type: 'delete',
      title: '确认删除角色',
      description: `将从组织“${selectedOrganization.org.name}”移除 ${selectedRoleIds.length} 个角色，是否继续？`,
      confirmText: '删除',
      cancelText: '取消',
      onConfirm: () =>
        updateCurrentRoles(currentRoles.filter((role) => !selectedRoleIds.includes(role.id))),
    });
  };

  const confirmSave = () => {
    if (!dirty || !workspaceQuery.data) return;
    let addedCount = 0;
    let removedCount = 0;
    for (const organization of organizations) {
      const orgId = organization.org.id;
      const initialIds = new Set((initialAssignments[orgId] ?? []).map((role) => role.id));
      const nextIds = new Set((assignments[orgId] ?? []).map((role) => role.id));
      for (const roleId of nextIds) if (!initialIds.has(roleId)) addedCount += 1;
      for (const roleId of initialIds) if (!nextIds.has(roleId)) removedCount += 1;
    }
    void confirmOperation({
      type: 'normal',
      title: '确认保存角色分配',
      description: `将为用户“${workspaceQuery.data.name}”新增 ${addedCount} 个角色关系、移除 ${removedCount} 个角色关系，是否保存？`,
      confirmText: '保存',
      cancelText: '取消',
      onConfirm: () => mutation.mutateAsync(),
    });
  };

  const organizationColumns: ColumnsType<UserRoleOrganizationVO> = [
    { title: '编码', dataIndex: ['org', 'number'], width: 130 },
    {
      title: '组织',
      dataIndex: ['org', 'name'],
      render: (name, organization) => (
        <span>
          {name}
          {organization.isPrimary && <span className="sm-user-role-primary-badge">主职</span>}
        </span>
      ),
    },
    { title: '职位', dataIndex: 'position', width: 150, render: (position) => position || '—' },
    {
      title: '角色数',
      width: 72,
      align: 'right',
      render: (_value, organization) => assignments[organization.org.id]?.length ?? 0,
    },
  ];

  return (
    <AssignmentPage
      access={{
        prefix: userAccess.prefix,
        permissions: { save: userAccess.permissions.assignRoles },
      }}
      loading={workspaceQuery.isLoading}
      saving={mutation.isPending}
      error={workspaceQuery.error as Error | null}
      dirty={dirty}
      saveDisabled={!dirty || mutation.isPending}
      closeGuard={{ appNumber, tabKey }}
      onRetry={() => void workspaceQuery.refetch()}
      onSave={confirmSave}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    >
      {workspaceQuery.data && (
        <div className="sm-user-role-workspace">
          <Descriptions
            className="sm-user-role-summary"
            size="small"
            column={3}
            items={[
              { key: 'name', label: '姓名', children: workspaceQuery.data.name },
              { key: 'username', label: '账号', children: workspaceQuery.data.username },
              { key: 'number', label: '工号', children: workspaceQuery.data.number },
            ]}
          />
          <Splitter className="sm-user-role-split">
            <Splitter.Panel defaultSize="46%" min="36%" max="60%">
              <section className="sm-user-role-panel">
                <div className="sm-user-role-panel-header">
                  <h2>任职组织</h2>
                  <span>共 {organizations.length} 个</span>
                </div>
                <div className="sm-user-role-table-body">
                  <Table<UserRoleOrganizationVO>
                    className="sm-user-role-organization-table"
                    rowKey={(organization) => organization.org.id}
                    size="small"
                    sticky
                    pagination={false}
                    columns={organizationColumns}
                    dataSource={organizations}
                    scroll={{ x: 'max-content', y: 1 }}
                    rowClassName={(organization) =>
                      organization.org.id === effectiveOrgId ? 'sm-user-role-selected-row' : ''
                    }
                    onRow={(organization) => ({
                      title: organization.orgNamePath,
                      onClick: () => {
                        setSelectedOrgId(organization.org.id);
                        setSelectedRoleIds([]);
                      },
                    })}
                  />
                </div>
              </section>
            </Splitter.Panel>
            <Splitter.Panel>
              <section className="sm-user-role-panel">
                <div className="sm-user-role-panel-header sm-user-role-role-header">
                  <div>
                    <h2>{selectedOrganization?.org.name ?? '已分配角色'}</h2>
                    <span>{selectedOrganization?.orgNamePath}</span>
                  </div>
                  <div className="sm-user-role-actions">
                    <Input.Search
                      allowClear
                      value={roleKeyword}
                      placeholder="搜索角色编码/名称/描述"
                      onChange={(event) => setRoleKeyword(event.target.value)}
                    />
                    <RefSelector
                      {...roleRefSelector}
                      value={null}
                      onChange={addSelectedRoles}
                      disabled={!effectiveOrgId}
                      trigger={
                        <Button type="primary" disabled={!effectiveOrgId}>
                          增加
                        </Button>
                      }
                    />
                    <Button
                      danger
                      disabled={selectedRoleIds.length === 0}
                      onClick={deleteSelectedRoles}
                    >
                      删除
                    </Button>
                  </div>
                </div>
                <div className="sm-user-role-table-body">
                  <Table<RoleSelectVO>
                    className="sm-assignment-table"
                    rowKey="id"
                    size="small"
                    sticky
                    pagination={false}
                    columns={roleColumns}
                    dataSource={visibleAssignedRoles}
                    scroll={{ x: 'max-content', y: 1 }}
                    rowSelection={{
                      selectedRowKeys: selectedRoleIds,
                      columnWidth: 36,
                      onChange: (keys) => setSelectedRoleIds(keys.map(String)),
                    }}
                  />
                </div>
              </section>
            </Splitter.Panel>
          </Splitter>
        </div>
      )}
    </AssignmentPage>
  );
};

export default UserRoleAssignmentPage;
