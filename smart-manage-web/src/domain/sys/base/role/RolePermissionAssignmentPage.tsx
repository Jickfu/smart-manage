import { useMemo, useState } from 'react';
import { Button, Checkbox, Table } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { AssignmentPage } from '@/domain/common/page/AssignmentPage';
import { AssignmentSelectionPanel } from '@/domain/common/page/AssignmentSelectionPanel';
import {
  getAssignmentSelectionDiff,
  replaceAssignmentScope,
} from '@/domain/common/page/assignmentSelection';
import ListTree from '@/domain/common/page/ListTree';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { permissionApi } from '@/domain/sys/base/permission/api';
import { permissionQueryKeys } from '@/domain/sys/base/permission/queryKeys';
import type { PermissionListAllVO } from '@/domain/sys/base/permission/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { roleApi } from './api';
import {
  buildPermissionAssignmentTree,
  filterPermissionsByAssignmentScope,
  getPermissionAssignmentScopeLabel,
  parsePermissionAssignmentScope,
  permissionAssignmentScopeKey,
} from './permissionAssignment';
import type { PermissionAssignmentScope } from './permissionAssignment';
import { roleAccess } from './permissions';
import { roleQueryKeys } from './queryKeys';
import type { RoleDetailVO } from './types';

const EMPTY_IDS: string[] = [];
const EMPTY_PERMISSIONS: PermissionListAllVO[] = [];

/** 角色权限分配专用页面。 */
const RolePermissionAssignmentPage = ({ appNumber, tabKey, billId }: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [localIds, setLocalIds] = useState<string[] | null>(null);
  const [scope, setScope] = useState<PermissionAssignmentScope>({ type: 'all' });
  const [keyword, setKeyword] = useState('');
  const [onlySelected, setOnlySelected] = useState(false);
  const detailQuery = useQuery({
    queryKey: roleQueryKeys.detail(billId),
    queryFn: () => roleApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const permissionsQuery = useQuery({
    queryKey: permissionQueryKeys.listAll(),
    queryFn: permissionApi.listAll,
  });
  const initialIds = detailQuery.data?.permissionIds ?? EMPTY_IDS;
  const checkedIds = localIds ?? initialIds;
  const checkedIdSet = useMemo(() => new Set(checkedIds), [checkedIds]);
  const permissions = permissionsQuery.data ?? EMPTY_PERMISSIONS;
  const treeData = useMemo(
    () => buildPermissionAssignmentTree(permissions, checkedIdSet),
    [checkedIdSet, permissions],
  );
  const scopePermissions = useMemo(
    () => filterPermissionsByAssignmentScope(permissions, scope),
    [permissions, scope],
  );
  const visiblePermissions = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();
    return scopePermissions.filter((permission) => {
      if (onlySelected && !checkedIdSet.has(permission.id)) return false;
      if (!normalizedKeyword) return true;
      return [
        permission.number,
        permission.name,
        permission.appName,
        permission.featureName,
        permission.featureKey,
      ]
        .filter(Boolean)
        .some((value) => value!.toLowerCase().includes(normalizedKeyword));
    });
  }, [checkedIdSet, keyword, onlySelected, scopePermissions]);
  const visibleIds = visiblePermissions.map((permission) => permission.id);
  const visibleSelectedCount = visibleIds.filter((id) => checkedIdSet.has(id)).length;
  const allVisibleSelected = visibleIds.length > 0 && visibleSelectedCount === visibleIds.length;
  const selectionDiff = useMemo(
    () => getAssignmentSelectionDiff(initialIds, checkedIds),
    [checkedIds, initialIds],
  );
  const dirty = selectionDiff.addedIds.length > 0 || selectionDiff.removedIds.length > 0;
  const columns = useMemo<ColumnsType<PermissionListAllVO>>(() => {
    const result: ColumnsType<PermissionListAllVO> = [
      {
        title: '#',
        width: 44,
        align: 'center',
        className: 'sm-assignment-sequence-column',
        render: (_value, _record, index) => index + 1,
      },
      { title: '权限编码', dataIndex: 'number', width: 280 },
      { title: '权限名称', dataIndex: 'name' },
    ];
    if (scope.type === 'all' || scope.type === 'app') {
      result.push({
        title: '所属功能',
        dataIndex: 'featureName',
        width: 180,
        render: (featureName) => featureName ?? '应用级权限',
      });
    }
    if (scope.type === 'all') {
      result.push({ title: '所属应用', dataIndex: 'appName', width: 160 });
    }
    return result;
  }, [scope.type]);
  const mutation = useCommandMutation({
    mutationFn: () => roleApi.assignPermissions(billId!, checkedIds),
    successMessage: '权限分配成功',
    onSuccess: async () => {
      queryClient.setQueryData<RoleDetailVO>(roleQueryKeys.detail(billId), (detail) =>
        detail ? { ...detail, permissionIds: [...checkedIds] } : detail,
      );
      setLocalIds(null);
      await queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(billId) });
    },
  });

  const updateVisibleSelection = (selected: boolean) => {
    setLocalIds(replaceAssignmentScope(checkedIds, visibleIds, selected ? visibleIds : []));
  };

  const confirmSave = () => {
    if (!dirty || !detailQuery.data) return;
    void confirmOperation({
      type: 'normal',
      title: '确认分配权限',
      description: `将为角色“${detailQuery.data.name}”新增 ${selectionDiff.addedIds.length} 项权限、移除 ${selectionDiff.removedIds.length} 项权限，是否保存？`,
      confirmText: '保存',
      cancelText: '取消',
      onConfirm: () => mutation.mutateAsync(),
    });
  };

  return (
    <AssignmentPage
      access={{
        prefix: roleAccess.prefix,
        permissions: { save: roleAccess.permissions.assignPermissions },
      }}
      loading={detailQuery.isLoading || permissionsQuery.isLoading}
      saving={mutation.isPending}
      error={(detailQuery.error || permissionsQuery.error) as Error | null}
      subject={
        detailQuery.data ? `角色：${detailQuery.data.number} — ${detailQuery.data.name}` : undefined
      }
      selectedCount={checkedIds.length}
      totalCount={permissions.length}
      dirty={dirty}
      saveDisabled={!dirty || mutation.isPending}
      closeGuard={{ appNumber, tabKey }}
      onRetry={() => void Promise.all([detailQuery.refetch(), permissionsQuery.refetch()])}
      onSave={confirmSave}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    >
      <AssignmentSelectionPanel
        title="权限选择"
        keyword={keyword}
        keywordPlaceholder="搜索权限编码/名称/应用/功能"
        onlySelected={onlySelected}
        meta={`${getPermissionAssignmentScopeLabel(permissions, scope)}：当前显示 ${visiblePermissions.length} 项，已选 ${visibleSelectedCount} 项`}
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
        treePanel={
          <ListTree
            blockNode
            virtual={false}
            treeData={treeData}
            defaultExpandedKeys={['all']}
            selectedKeys={[permissionAssignmentScopeKey(scope)]}
            onSelect={(keys) => setScope(parsePermissionAssignmentScope(keys[0] ?? 'all'))}
          />
        }
        onKeywordChange={setKeyword}
        onOnlySelectedChange={setOnlySelected}
      >
        <Table<PermissionListAllVO>
          className="sm-assignment-table"
          rowKey="id"
          size="small"
          sticky
          pagination={false}
          columns={columns}
          dataSource={visiblePermissions}
          scroll={{ x: 'max-content', y: 1 }}
          onRow={(permission) => ({
            onClick: () => {
              const selected = !checkedIdSet.has(permission.id);
              setLocalIds(
                replaceAssignmentScope(
                  checkedIds,
                  [permission.id],
                  selected ? [permission.id] : [],
                ),
              );
            },
          })}
          rowSelection={{
            selectedRowKeys: checkedIds,
            preserveSelectedRowKeys: true,
            columnWidth: 36,
            onSelect: (permission, selected) => {
              setLocalIds(
                replaceAssignmentScope(
                  checkedIds,
                  [permission.id],
                  selected ? [permission.id] : [],
                ),
              );
            },
            onSelectAll: (selected, _selectedRows, changedRows) => {
              const changedIds = changedRows.map((permission) => permission.id);
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

export default RolePermissionAssignmentPage;
