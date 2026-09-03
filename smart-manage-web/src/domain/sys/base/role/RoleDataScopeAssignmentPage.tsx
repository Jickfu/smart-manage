import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useMemo, useRef, useState } from 'react';
import type { Key } from 'react';
import { Form, Input, Select } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { orgApi } from '@/domain/sys/base/org/api';
import type { OrgOptionVO } from '@/domain/sys/base/org/types';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { AssignmentPage } from '@/domain/common/page/assignment/AssignmentPage';
import { EditSectionCollapse } from '@/domain/common/page/edit/EditSectionCollapse';
import { EditSectionActionButton } from '@/domain/common/page/edit/EditSectionActionButton';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import {
  EditableDetailTable,
  RequiredDetailColumnTitle,
} from '@/domain/common/component/EditableDetailTable';
import RefSelector from '@/domain/common/component/RefSelector';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useWorkbenchStore } from '@/stores/workbench';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { DataScopeType, RoleDataScopeRule, RoleDataScopeWorkspace } from './types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
import { roleAccess } from './permissions';
import { isDataScopeAssignmentDirty } from './dataScopeAssignment';
import {
  createDataScopeRuleDraft,
  projectDataScopeRules,
  queryDataScopeOrganizations,
  resolveDataScopeOrganizations,
  validateDataScopeRules,
} from './dataScopeRuleEditor';
import type {
  DataScopeOrgRecord,
  DataScopeRuleDraft,
  DataScopeRuleError,
} from './dataScopeRuleEditor';
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
  const feedback = useOperationFeedback();
  const [defaultScope, setDefaultScope] = useState<DataScopeType>(workspace.defaultDataScope);
  const [drafts, setDrafts] = useState(() => workspace.rules.map(createDataScopeRuleDraft));
  const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
  const [validationAttempted, setValidationAttempted] = useState(false);
  const tableRef = useRef<HTMLDivElement>(null);
  const rules = useMemo(() => projectDataScopeRules(drafts), [drafts]);
  const dirty = isDataScopeAssignmentDirty(workspace, { defaultDataScope: defaultScope, rules });
  const validationErrors = validationAttempted
    ? validateDataScopeRules(rules, workspace.resources)
    : [];
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
  const disabled = Boolean(error) || loading || mutation.isPending;
  const updateRule = (localKey: string, patch: Partial<RoleDataScopeRule>) =>
    setDrafts((current) =>
      current.map((rule) => (rule.localKey === localKey ? { ...rule, ...patch } : rule)),
    );
  const confirmSave = () => {
    if (disabled || !dirty) return;
    setValidationAttempted(true);
    const errors = validateDataScopeRules(rules, workspace.resources);
    if (errors.length) {
      feedback.warning(errors[0]!.message);
      // 等字段错误状态完成渲染后聚焦，沿用明细表的浮动错误文案。
      requestAnimationFrame(() =>
        tableRef.current
          ?.querySelector<HTMLInputElement>('.ant-form-item-has-error input')
          ?.focus(),
      );
      return;
    }
    void confirmOperation({
      type: 'warning',
      title: '确认分配数据范围',
      description: `将修改角色“${workspace.roleNumber} — ${workspace.roleName}”的数据访问范围，影响该角色用户可访问的业务数据，是否保存？`,
      confirmText: '保存',
      cancelText: '取消',
      onConfirm: () => mutation.mutateAsync(),
    });
  };
  const fieldError = (index: number, field: DataScopeRuleError['field']) => {
    const message = validationErrors.find(
      (entry) => entry.index === index && entry.field === field,
    )?.message;
    return { validateStatus: message ? ('error' as const) : undefined, help: message };
  };
  const columns: ColumnsType<DataScopeRuleDraft> = [
    {
      title: <RequiredDetailColumnTitle>资源</RequiredDetailColumnTitle>,
      width: 320,
      render: (_value, rule, index) => (
        <Form.Item {...fieldError(index, 'resourceType')}>
          <Select
            aria-label={`第${index + 1}行资源`}
            variant="borderless"
            disabled={disabled}
            value={rule.resourceType}
            options={resourceOptions}
            onChange={(resourceType) =>
              updateRule(rule.localKey, { resourceType, action: undefined })
            }
          />
        </Form.Item>
      ),
    },
    {
      title: '操作',
      width: 240,
      render: (_value, rule, index) => (
        <Form.Item {...fieldError(index, 'action')}>
          <Select
            aria-label={`第${index + 1}行操作`}
            variant="borderless"
            disabled={disabled}
            allowClear
            placeholder="资源默认（覆盖角色默认）"
            value={rule.action}
            options={(workspace.resources[rule.resourceType] ?? []).map((value) => ({
              value,
              label: value,
            }))}
            onChange={(action) => updateRule(rule.localKey, { action })}
          />
        </Form.Item>
      ),
    },
    {
      title: <RequiredDetailColumnTitle>数据范围</RequiredDetailColumnTitle>,
      width: 170,
      render: (_value, rule, index) => (
        <Form.Item>
          <Select
            aria-label={`第${index + 1}行数据范围`}
            variant="borderless"
            disabled={disabled}
            value={rule.scopeType}
            options={scopeOptions}
            onChange={(scopeType) => updateRule(rule.localKey, { scopeType })}
          />
        </Form.Item>
      ),
    },
    {
      title: '指定组织',
      render: (_value, rule, index) =>
        rule.scopeType === 'CUSTOM_ORGS' ? (
          <Form.Item {...fieldError(index, 'orgIds')}>
            <RefSelector<DataScopeOrgRecord>
              selectorKey={[
                'role-data-scope-organizations',
                workspace.roleId,
                rule.localKey,
                organizations,
              ]}
              mode="multiple"
              modalTitle="选择数据范围组织"
              placeholder="请选择组织"
              disabled={disabled}
              value={resolveDataScopeOrganizations(rule.orgIds, organizations)}
              onChange={(selected) =>
                updateRule(rule.localKey, {
                  orgIds: (Array.isArray(selected) ? selected : []).map(
                    (organization) => organization.id,
                  ),
                })
              }
              fetchFn={async (params) => queryDataScopeOrganizations(organizations, params)}
              fieldNames={{ key: 'id', label: 'name' }}
              displayRender={(organization) => organization.namePath}
              columns={[
                { title: '编码', dataIndex: 'number', width: 160 },
                { title: '名称', dataIndex: 'name', width: 180 },
                { title: '长名称', dataIndex: 'namePath' },
              ]}
            />
          </Form.Item>
        ) : null,
    },
  ];
  return (
    <AssignmentPage
      access={assignmentAccess}
      loading={loading}
      saving={mutation.isPending}
      error={error}
      showHeaderContext={false}
      dirty={dirty}
      saveDisabled={disabled || !dirty}
      closeGuard={{ appNumber, tabKey }}
      onRetry={onRetry}
      onSave={confirmSave}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    >
      <Form layout="vertical" className="sm-edit-form">
        <EditSectionCollapse
          defaultActiveKeys={['basic', 'rules']}
          items={[
            {
              key: 'basic',
              label: '基本信息',
              children: (
                <FormFieldGrid>
                  <FormFieldCell>
                    <Form.Item className="sm-edit-field-content" label="角色编码">
                      <Input variant="underlined" value={workspace.roleNumber} readOnly />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item className="sm-edit-field-content" label="角色名称">
                      <Input variant="underlined" value={workspace.roleName} readOnly />
                    </Form.Item>
                  </FormFieldCell>
                  <FormFieldCell>
                    <Form.Item className="sm-edit-field-content" label="默认数据范围">
                      <Select
                        aria-label="默认数据范围"
                        variant="underlined"
                        value={defaultScope}
                        options={scopeOptions.filter((option) => option.value !== 'CUSTOM_ORGS')}
                        onChange={setDefaultScope}
                        disabled={disabled}
                      />
                    </Form.Item>
                  </FormFieldCell>
                </FormFieldGrid>
              ),
            },
            {
              key: 'rules',
              label: '资源例外配置',
              extra: (
                <div className="sm-data-scope-actions">
                  <EditSectionActionButton
                    disabled={disabled || resourceOptions.length === 0}
                    onClick={() =>
                      setDrafts((current) => [
                        ...current,
                        createDataScopeRuleDraft({
                          resourceType: resourceOptions[0]!.value,
                          scopeType: 'ORG',
                          orgIds: [],
                        }),
                      ])
                    }
                  >
                    新增
                  </EditSectionActionButton>
                  <EditSectionActionButton
                    danger
                    disabled={disabled || selectedRowKeys.length === 0}
                    onClick={() => {
                      setDrafts((current) =>
                        current.filter((rule) => !selectedRowKeys.includes(rule.localKey)),
                      );
                      setSelectedRowKeys([]);
                    }}
                  >
                    删除
                  </EditSectionActionButton>
                </div>
              ),
              children: (
                <div ref={tableRef}>
                  <EditableDetailTable
                    editable={!disabled}
                    columns={columns}
                    dataSource={drafts}
                    rowKey="localKey"
                    selectedRowKeys={selectedRowKeys}
                    onSelectedRowKeysChange={setSelectedRowKeys}
                  />
                </div>
              ),
            },
          ]}
        />
      </Form>
    </AssignmentPage>
  );
};

const RoleDataScopeAssignmentPage = (props: PageComponentProps) => {
  const workspaceQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: [...roleQueryKeys.detail(props.billId), 'data-scopes'],
    queryFn: () => roleApi.dataScopeWorkspace(props.billId!),
    enabled: Boolean(props.billId),
  });
  const orgQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: ['sys', 'base', 'org', 'options'],
    queryFn: orgApi.options,
  });
  const loading = workspaceQuery.isLoading || orgQuery.isLoading;
  const error = getBlockingQueryError(workspaceQuery) ?? getBlockingQueryError(orgQuery);
  const onRetry = () =>
    void Promise.all([workspaceQuery.isEnabled && workspaceQuery.refetch(), orgQuery.refetch()]);
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
      showHeaderContext={false}
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
