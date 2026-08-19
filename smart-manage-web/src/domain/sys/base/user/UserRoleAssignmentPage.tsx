import { useMemo, useState } from 'react';
import { App, Button, Checkbox, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { AssignmentPage } from '@/domain/common/page/AssignmentPage';
import { AssignmentSelectionPanel } from '@/domain/common/page/AssignmentSelectionPanel';
import {
  getAssignmentSelectionDiff,
  replaceAssignmentScope,
} from '@/domain/common/page/assignmentSelection';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { roleApi } from '@/domain/sys/base/role/api';
import { roleQueryKeys } from '@/domain/sys/base/role/queryKeys';
import type { RoleListAllVO } from '@/domain/sys/base/role/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { userApi } from './api';
import { userAccess } from './permissions';
import { userQueryKeys } from './queryKeys';
import type { UserDetailVO } from './types';

const EMPTY_IDS: string[] = [];
const EMPTY_ROLES: RoleListAllVO[] = [];

const roleColumns: ColumnsType<RoleListAllVO> = [
  {
    title: '#',
    width: 44,
    align: 'center',
    className: 'sm-assignment-sequence-column',
    render: (_value, _record, index) => index + 1,
  },
  { title: '编码', dataIndex: 'number', width: 220 },
  { title: '名称', dataIndex: 'name', width: 220 },
  { title: '说明', dataIndex: 'description', render: (description) => description || '—' },
];

/** 用户角色分配专用页面。 */
const UserRoleAssignmentPage = ({ appNumber, tabKey, billId }: PageComponentProps) => {
  const { modal } = App.useApp();
  const queryClient = useQueryClient();
  const [localIds, setLocalIds] = useState<string[] | null>(null);
  const [keyword, setKeyword] = useState('');
  const [onlySelected, setOnlySelected] = useState(false);
  const detailQuery = useQuery({
    queryKey: userQueryKeys.detail(billId),
    queryFn: () => userApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const rolesQuery = useQuery({ queryKey: roleQueryKeys.listAll(), queryFn: roleApi.listAll });
  const initialIds = detailQuery.data?.roleIds ?? EMPTY_IDS;
  const checkedIds = localIds ?? initialIds;
  const checkedIdSet = useMemo(() => new Set(checkedIds), [checkedIds]);
  const selectionDiff = useMemo(
    () => getAssignmentSelectionDiff(initialIds, checkedIds),
    [checkedIds, initialIds],
  );
  const dirty = selectionDiff.addedIds.length > 0 || selectionDiff.removedIds.length > 0;
  const roles = rolesQuery.data ?? EMPTY_ROLES;
  const visibleRoles = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return roles.filter((role) => {
      if (onlySelected && !checkedIdSet.has(role.id)) return false;
      if (!normalizedKeyword) return true;
      return [role.number, role.name, role.description]
        .filter(Boolean)
        .some((value) => value!.toLowerCase().includes(normalizedKeyword));
    });
  }, [checkedIdSet, keyword, onlySelected, roles]);
  const visibleIds = visibleRoles.map((role) => role.id);
  const visibleSelectedCount = visibleIds.filter((id) => checkedIdSet.has(id)).length;
  const allVisibleSelected = visibleIds.length > 0 && visibleSelectedCount === visibleIds.length;
  const mutation = useCommandMutation({
    mutationFn: () => userApi.assignRoles(billId!, checkedIds),
    successMessage: '角色分配成功',
    onSuccess: async () => {
      queryClient.setQueryData<UserDetailVO>(userQueryKeys.detail(billId), (detail) =>
        detail ? { ...detail, roleIds: [...checkedIds] } : detail,
      );
      setLocalIds(null);
      await queryClient.invalidateQueries({ queryKey: userQueryKeys.detail(billId) });
    },
  });

  const updateVisibleSelection = (selected: boolean) => {
    setLocalIds(replaceAssignmentScope(checkedIds, visibleIds, selected ? visibleIds : []));
  };

  const confirmSave = () => {
    if (!dirty || !detailQuery.data) return;
    modal.confirm({
      title: '确认分配角色',
      content: `将为用户“${detailQuery.data.name}”新增 ${selectionDiff.addedIds.length} 个角色、移除 ${selectionDiff.removedIds.length} 个角色，是否保存？`,
      okText: '保存',
      cancelText: '取消',
      onOk: () => mutation.mutateAsync(),
    });
  };

  return (
    <AssignmentPage
      access={{
        prefix: userAccess.prefix,
        permissions: { save: userAccess.permissions.assignRoles },
      }}
      loading={detailQuery.isLoading || rolesQuery.isLoading}
      saving={mutation.isPending}
      error={(detailQuery.error || rolesQuery.error) as Error | null}
      subject={
        detailQuery.data
          ? `用户：${detailQuery.data.name}（${detailQuery.data.username} / ${detailQuery.data.number}）`
          : undefined
      }
      selectedCount={checkedIds.length}
      totalCount={roles.length}
      dirty={dirty}
      saveDisabled={!dirty || mutation.isPending}
      closeGuard={{ appNumber, tabKey }}
      onRetry={() => void Promise.all([detailQuery.refetch(), rolesQuery.refetch()])}
      onSave={confirmSave}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    >
      <AssignmentSelectionPanel
        title="角色选择"
        keyword={keyword}
        keywordPlaceholder="搜索角色编码/名称/说明"
        onlySelected={onlySelected}
        meta={`当前显示 ${visibleRoles.length} 个角色，已选 ${visibleSelectedCount} 个`}
        actions={
          <>
            <Checkbox
              checked={allVisibleSelected}
              indeterminate={visibleSelectedCount > 0 && !allVisibleSelected}
              disabled={visibleIds.length === 0}
              onChange={(event) => updateVisibleSelection(event.target.checked)}
            >
              全选当前结果
            </Checkbox>
            <Button
              type="link"
              size="small"
              disabled={visibleSelectedCount === 0}
              onClick={() => updateVisibleSelection(false)}
            >
              清空当前结果
            </Button>
          </>
        }
        onKeywordChange={setKeyword}
        onOnlySelectedChange={setOnlySelected}
      >
        <Table<RoleListAllVO>
          className="sm-assignment-table"
          rowKey="id"
          size="small"
          sticky
          pagination={false}
          columns={roleColumns}
          dataSource={visibleRoles}
          scroll={{ x: 'max-content', y: 1 }}
          onRow={(role) => ({
            onClick: () => {
              const selected = !checkedIdSet.has(role.id);
              setLocalIds(replaceAssignmentScope(checkedIds, [role.id], selected ? [role.id] : []));
            },
          })}
          rowSelection={{
            selectedRowKeys: checkedIds,
            preserveSelectedRowKeys: true,
            columnWidth: 36,
            onSelect: (role, selected) => {
              setLocalIds(replaceAssignmentScope(checkedIds, [role.id], selected ? [role.id] : []));
            },
            onSelectAll: (selected, _selectedRows, changedRows) => {
              const changedIds = changedRows.map((role) => role.id);
              setLocalIds(
                replaceAssignmentScope(checkedIds, changedIds, selected ? changedIds : []),
              );
            },
          }}
        />
      </AssignmentSelectionPanel>
    </AssignmentPage>
  );
};

export default UserRoleAssignmentPage;
