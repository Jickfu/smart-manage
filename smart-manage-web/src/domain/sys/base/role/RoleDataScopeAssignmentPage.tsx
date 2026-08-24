import { useMemo, useState } from 'react';
import { Button, Card, Form, Result, Select, Spin } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { orgApi } from '@/domain/sys/base/org/api';
import type { OrgOptionVO } from '@/domain/sys/base/org/types';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import type { PageComponentProps } from '@/domain/common/page/types';
import type { DataScopeType, RoleDataScopeRule, RoleDataScopeWorkspace } from './types';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';
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
}

const DataScopeEditor = ({ appNumber, tabKey, workspace, organizations, refresh }: EditorProps) => {
  const queryClient = useQueryClient();
  const [defaultScope, setDefaultScope] = useState<DataScopeType>(workspace.defaultDataScope);
  const [rules, setRules] = useState<RoleDataScopeRule[]>(workspace.rules);
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
      await queryClient.invalidateQueries({ queryKey: roleQueryKeys.detail(workspace.roleId) });
      await refresh();
    },
  });
  const updateRule = (index: number, patch: Partial<RoleDataScopeRule>) =>
    setRules((current) =>
      current.map((rule, ruleIndex) => (ruleIndex === index ? { ...rule, ...patch } : rule)),
    );
  return (
    <>
      <Card title={`角色：${workspace.roleNumber} — ${workspace.roleName}`}>
        <Form layout="vertical">
          <Form.Item label="默认数据范围">
            <Select
              value={defaultScope}
              options={scopeOptions.filter((option) => option.value !== 'CUSTOM_ORGS')}
              onChange={setDefaultScope}
              disabled={workspace.roleNumber === 'admin'}
            />
          </Form.Item>
        </Form>
      </Card>
      <Card
        title="资源例外配置"
        extra={
          <Button
            type="primary"
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
                  value={rule.resourceType}
                  options={resourceOptions}
                  onChange={(resourceType) =>
                    updateRule(index, { resourceType, action: undefined })
                  }
                />
                <Select
                  allowClear
                  placeholder="资源默认（覆盖角色默认）"
                  value={rule.action}
                  options={actions.map((value) => ({ value, label: value }))}
                  onChange={(action) => updateRule(index, { action })}
                />
                <Select
                  value={rule.scopeType}
                  options={scopeOptions}
                  onChange={(scopeType) => updateRule(index, { scopeType })}
                />
                {rule.scopeType === 'CUSTOM_ORGS' && (
                  <Select
                    mode="multiple"
                    placeholder="选择组织"
                    value={rule.orgIds}
                    options={organizations.map((org) => ({ value: org.id, label: org.namePath }))}
                    onChange={(orgIds) => updateRule(index, { orgIds })}
                  />
                )}
                <Button
                  danger
                  onClick={() =>
                    setRules((current) => current.filter((_item, ruleIndex) => ruleIndex !== index))
                  }
                >
                  删除
                </Button>
              </div>
            );
          })}
        </div>
      </Card>
      <div className="sm-data-scope-actions">
        <Button
          type="primary"
          loading={mutation.isPending}
          disabled={workspace.roleNumber === 'admin'}
          onClick={() => mutation.mutate()}
        >
          保存
        </Button>
        <Button
          type="primary"
          onClick={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
        >
          退出
        </Button>
      </div>
    </>
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
  if (workspaceQuery.error || orgQuery.error)
    return <Result status="error" title="数据范围配置加载失败" />;
  return (
    <div className="sm-data-scope-page">
      <Spin spinning={workspaceQuery.isLoading || orgQuery.isLoading}>
        {workspaceQuery.data && orgQuery.data && (
          <DataScopeEditor
            key={workspaceQuery.data.version}
            {...props}
            workspace={workspaceQuery.data}
            organizations={orgQuery.data}
            refresh={workspaceQuery.refetch}
          />
        )}
      </Spin>
    </div>
  );
};

export default RoleDataScopeAssignmentPage;
