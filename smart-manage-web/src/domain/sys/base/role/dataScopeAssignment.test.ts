import { describe, expect, it } from 'vitest';
import { isDataScopeAssignmentDirty } from './dataScopeAssignment';
import type { DataScopeAssignmentState } from './dataScopeAssignment';
import type { RoleDataScopeRule } from './types';

describe('角色数据范围脏状态', () => {
  const resourceRule: RoleDataScopeRule = {
    resourceType: 'purchase',
    scopeType: 'CUSTOM_ORGS',
    orgIds: ['two', 'one'],
  };
  const actionRule: RoleDataScopeRule = {
    resourceType: 'purchase',
    action: 'read',
    scopeType: 'SELF',
    orgIds: [],
  };
  const baseline: DataScopeAssignmentState = {
    defaultDataScope: 'SELF',
    rules: [resourceRule, actionRule],
  };

  it('初始状态及改回原值不脏', () => {
    expect(isDataScopeAssignmentDirty(baseline, baseline)).toBe(false);
    expect(isDataScopeAssignmentDirty(baseline, { ...baseline, defaultDataScope: 'ORG' })).toBe(
      true,
    );
    expect(isDataScopeAssignmentDirty(baseline, { ...baseline, defaultDataScope: 'SELF' })).toBe(
      false,
    );
  });

  it('忽略规则顺序、组织顺序和重复组织，但不修改输入', () => {
    const current: DataScopeAssignmentState = {
      defaultDataScope: 'SELF',
      rules: [actionRule, { ...resourceRule, orgIds: ['one', 'two', 'one'] }],
    };
    const before = JSON.stringify(current);
    expect(isDataScopeAssignmentDirty(baseline, current)).toBe(false);
    expect(JSON.stringify(current)).toBe(before);
    expect(resourceRule.orgIds).toEqual(['two', 'one']);
  });

  it('缺失 action 与空 action 同属资源默认规则', () => {
    expect(
      isDataScopeAssignmentDirty(baseline, {
        ...baseline,
        rules: [{ ...resourceRule, action: '' }, actionRule],
      }),
    ).toBe(false);
  });

  it('增删规则、修改身份、范围或组织都会变脏', () => {
    const changes: RoleDataScopeRule[][] = [
      [resourceRule],
      [...baseline.rules, resourceRule],
      [{ ...resourceRule, resourceType: 'other' }, actionRule],
      [resourceRule, { ...actionRule, action: 'write' }],
      [resourceRule, { ...actionRule, scopeType: 'ALL' }],
      [{ ...resourceRule, orgIds: ['one'] }, actionRule],
    ];
    for (const rules of changes) {
      expect(isDataScopeAssignmentDirty(baseline, { ...baseline, rules })).toBe(true);
    }
  });

  it('非法重复规则不被合并，重复规则仅重排不改变状态', () => {
    const duplicate = { ...resourceRule, scopeType: 'ORG' as const };
    const duplicated = { ...baseline, rules: [resourceRule, duplicate] };
    expect(
      isDataScopeAssignmentDirty(duplicated, { ...baseline, rules: [duplicate, resourceRule] }),
    ).toBe(false);
    expect(isDataScopeAssignmentDirty(duplicated, { ...baseline, rules: [resourceRule] })).toBe(
      true,
    );
  });
});
