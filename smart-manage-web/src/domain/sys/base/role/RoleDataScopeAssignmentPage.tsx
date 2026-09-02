import { useMemo, useState } from 'react';
import { Button, Card, Form, Select } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { orgApi } from '@/domain/sys/base/org/api';
import type { OrgOptionVO } from '@/domain/sys/base/org/types';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { AssignmentPage } from '@/domain/common/page/AssignmentPage';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useWorkbenchStore } from '@/stores/workbench';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { DataScopeType, RoleDataScopeRule, RoleDataScopeWorkspace } from './types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import { roleAccess } from './permissions';
import { isDataScopeAssignmentDirty } from './dataScopeAssignment';
import './RoleDataScopeAssignmentPage.css';

const scopeOptions = [
  { label: '全部数据', value: 'ALL' },
  { label: '本组织及下级', value: 'ORG_AND_CHILDREN' },
  { label: '本组织', value: 'ORG' },
  { label: '本人相关', value: 'SELF' },
  { label: '自定义组织', value: 'CUSTOM_ORGS' },
];

interface EditorProps extends PageComponentProps {
  workspace: RoleDataScopeWorkspace;
  organizations: OrgOptionVO[];
  refresh: () => Promise<unknown>;
  loading: boolean;
  error: Error | null;
  onRetry: () => void;
}

const assignmentAccess = {
  prefix: roleAccess.prefix,
  permissions: { save: roleAccess.permissions.assignDataScopes },
};

const DataScopeEditor = ({
  appNumber,
  tabKey,
  workspace,
  organizations,
  refresh,
  loading,
  error,
  onRetry,
}: EditorProps) => {
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const [defaultScope, setDefaultScope] = useState<DataScopeType>(workspace.defaultDataScope);
  const [rules, setRules] = useState<RoleDataScopeRule[]>(workspace.rules);
  const readonly = workspace.roleNumber === 'admin';
  const dirty = isDataScopeAssignmentDirty(workspace, { defaultDataScope: defaultScope, rules });
  const resourceOptions = useMemo(
    () => Object.keys(workspace.resources).map((value) => ({ value, label: value })),
    [workspace.resources],
  );
  const mutation = useCommandMutation({
    mutationFn: () =>
      roleApi.assignDataScopes({
        roleId: workspace.roleId,
        version: workspace.version,
        defaultDataScope: defaultScope,
        rules,
      }),
    successMessage: '数据范围分配成功',
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries(
          { queryKey: roleQueryKeys.detail(workspace.roleId), exact: true },
          { throwOnError: true },
        ),
        refresh(),
      ]);
    },
  });
  const updateRule = (index: number, patch: Partial<RoleDataScopeRule>) =>
    setRules((current) =>
      current.map((rule, ruleIndex) => (ruleIndex === index ? { ...rule, ...patch } : rule)),
    );
  const confirmSave = () => {
    if (readonly || !dirty || mutation.isPending) return;
    void confirmOperation({
      type: 'warning',
      title: '确认分配数据范围',
      description: `将修改角色“${workspace.roleNumber} — ${workspace.roleName}”的数据访问范围，影响该角色用户可访问的业务数据，是否保存？`,
      confirmText: '保存',
      cancelText: '取消',
      onConfirm: () => mutation.mutateAsync(),
    });
  };
  const disabled = readonly || mutation.isPending;
  return (
    <AssignmentPage
      access={assignmentAccess}
      loading={loading}
      saving={mutation.isPending}
      error={error}
      subject={`角色：${workspace.roleNumber} — ${workspace.roleName}`}
      dirty={dirty}
      saveDisabled={disabled || !dirty}
      closeGuard={{ appNumber, tabKey }}
      onRetry={onRetry}
      onSave={confirmSave}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    >
      <div className="sm-data-scope-page">
        <Card title={`角色：${workspace.roleNumber} — ${workspace.roleName}`}>
          <Form layout="vertical">
            <Form.Item label="默认数据范围">
              <Select
                value={defaultScope}
                options={scopeOptions.filter((option) => option.value !== 'CUSTOM_ORGS')}
                onChange={setDefaultScope}
                disabled={disabled}
              />
            </Form.Item>
          </Form>
        </Card>
        <Card
          title="资源例外配置"
          extra={
            <Button
              type="primary"
              disabled={disabled}
              onClick={() =>
                setRules((current) => [
                  ...current,
                  { resourceType: resourceOptions[0]?.value ?? '', scopeType: 'ORG', orgIds: [] },
                ])
              }
            >
              添加规则
            </Button>
          }
        >
          <div className="sm-data-scope-rules">
            {rules.map((rule, index) => {
              const actions = workspace.resources[rule.resourceType] ?? [];
              return (
                <div
                  className="sm-data-scope-rule"
                  key={`${rule.resourceType}-${rule.action ?? 'default'}-${index}`}
                >
                  <Select
                    disabled={disabled}
                    value={rule.resourceType}
                    options={resourceOptions}
                    onChange={(resourceType) =>
                      updateRule(index, { resourceType, action: undefined })
                    }
                  />
                  <Select
                    disabled={disabled}
                    allowClear
                    placeholder="资源默认（覆盖角色默认）"
                    value={rule.action}
                    options={actions.map((value) => ({ value, label: value }))}
                    onChange={(action) => updateRule(index, { action })}
                  />
                  <Select
                    disabled={disabled}
                    value={rule.scopeType}
                    options={scopeOptions}
                    onChange={(scopeType) => updateRule(index, { scopeType })}
                  />
                  {rule.scopeType === 'CUSTOM_ORGS' && (
                    <Select
                      disabled={disabled}
                      mode="multiple"
                      placeholder="选择组织"
                      value={rule.orgIds}
                      options={organizations.map((org) => ({ value: org.id, label: org.namePath }))}
                      onChange={(orgIds) => updateRule(index, { orgIds })}
                    />
                  )}
                  <Button
                    danger
                    disabled={disabled}
                    onClick={() =>
                      setRules((current) =>
                        current.filter((_item, ruleIndex) => ruleIndex !== index),
                      )
                    }
                  >
                    删除
                  </Button>
                </div>
              );
            })}
          </div>
        </Card>
      </div>
    </AssignmentPage>
  );
};

const RoleDataScopeAssignmentPage = (props: PageComponentProps) => {
  const workspaceQuery = useQuery({
    queryKey: [...roleQueryKeys.detail(props.billId), 'data-scopes'],
    queryFn: () => roleApi.dataScopeWorkspace(props.billId!),
    enabled: Boolean(props.billId),
  });
  const orgQuery = useQuery({
    queryKey: ['sys', 'base', 'org', 'options'],
    queryFn: orgApi.options,
  });
  const loading = workspaceQuery.isLoading || orgQuery.isLoading;
  const error = workspaceQuery.error ?? orgQuery.error;
  const onRetry = () => void Promise.all([workspaceQuery.refetch(), orgQuery.refetch()]);
  if (workspaceQuery.data && orgQuery.data) {
    return (
      <DataScopeEditor
        key={`${workspaceQuery.data.roleId}:${workspaceQuery.data.version}`}
        {...props}
        workspace={workspaceQuery.data}
        organizations={orgQuery.data}
        refresh={() => workspaceQuery.refetch({ throwOnError: true })}
        loading={loading}
        error={error}
        onRetry={onRetry}
      />
    );
  }
  return (
    <AssignmentPage
      access={assignmentAccess}
      loading={loading}
      saving={false}
      error={error}
      saveDisabled
      onRetry={onRetry}
      onSave={() => {}}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    >
      {null}
    </AssignmentPage>
  );
};

export default RoleDataScopeAssignmentPage;
