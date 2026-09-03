import type { OrgOptionVO } from '../org/types';
import type { RoleDataScopeRule } from './types';

export interface DataScopeRuleDraft extends RoleDataScopeRule {
  localKey: string;
}

export interface DataScopeRuleError {
  index: number;
  field: 'resourceType' | 'action' | 'scopeType' | 'orgIds';
  message: string;
}

export function createDataScopeRuleDraft(rule: RoleDataScopeRule): DataScopeRuleDraft {
  return { ...rule, orgIds: [...rule.orgIds], localKey: crypto.randomUUID() };
}

/** 本地行身份不参与业务比较、校验或请求序列化。 */
export function projectDataScopeRules(drafts: readonly DataScopeRuleDraft[]): RoleDataScopeRule[] {
  return drafts.map(({ resourceType, action, scopeType, orgIds }) => ({
    resourceType,
    action,
    scopeType,
    orgIds: [...orgIds],
  }));
}

export function validateDataScopeRules(
  rules: readonly RoleDataScopeRule[],
  resources: Record<string, string[]>,
): DataScopeRuleError[] {
  const identities = new Set<string>();
  const errors: DataScopeRuleError[] = [];
  rules.forEach((rule, index) => {
    if (!Object.hasOwn(resources, rule.resourceType)) {
      errors.push({ index, field: 'resourceType', message: '请选择有效的资源' });
    } else if (rule.action && !resources[rule.resourceType]!.includes(rule.action)) {
      errors.push({ index, field: 'action', message: '请选择该资源支持的操作' });
    }
    const identity = JSON.stringify([rule.resourceType, rule.action ?? '']);
    if (identities.has(identity)) {
      errors.push({ index, field: 'action', message: '同一资源操作只能配置一条数据范围规则' });
    }
    identities.add(identity);
    if (rule.scopeType === 'CUSTOM_ORGS' && rule.orgIds.length === 0) {
      errors.push({ index, field: 'orgIds', message: '请选择至少一个组织' });
    }
  });
  return errors;
}

export type DataScopeOrgRecord = OrgOptionVO & Record<string, unknown>;

/** 候选集缺失不代表取消授权；占位记录只用于已选值，不加入候选列表。 */
export function resolveDataScopeOrganizations(
  orgIds: readonly string[],
  organizations: readonly OrgOptionVO[],
): DataScopeOrgRecord[] {
  const organizationsById = new Map(
    organizations.map((organization) => [organization.id, organization]),
  );
  return orgIds.map((id) => ({
    ...(organizationsById.get(id) ?? {
      id,
      number: '',
      name: `未解析组织（${id}）`,
      namePath: `未解析组织（${id}）`,
    }),
  }));
}

/** 本页沿用原 options 权限与候选范围，仅适配 RefSelector 的分页搜索协议。 */
export function queryDataScopeOrganizations(
  organizations: readonly OrgOptionVO[],
  { pageNum, pageSize, keyword }: { pageNum: number; pageSize: number; keyword?: string },
) {
  const normalizedKeyword = keyword?.trim().toLowerCase();
  const matched = normalizedKeyword
    ? organizations.filter((organization) =>
        [organization.number, organization.name, organization.namePath].some((value) =>
          value.toLowerCase().includes(normalizedKeyword),
        ),
      )
    : organizations;
  const start = (pageNum - 1) * pageSize;
  return {
    records: matched.slice(start, start + pageSize).map((organization) => ({ ...organization })),
    total: matched.length,
  };
}
