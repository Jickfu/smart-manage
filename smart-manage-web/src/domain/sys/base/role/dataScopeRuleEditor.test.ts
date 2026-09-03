import { describe, expect, it } from 'vitest';
import { isDataScopeAssignmentDirty } from './dataScopeAssignment';
import {
  createDataScopeRuleDraft,
  projectDataScopeRules,
  queryDataScopeOrganizations,
  resolveDataScopeOrganizations,
  validateDataScopeRules,
} from './dataScopeRuleEditor';

describe('数据范围规则边界', () => {
  it('本地身份不进入业务投影或脏状态，重复规则仍单独存在', () => {
    const rule = { resourceType: 'purchase', scopeType: 'ORG' as const, orgIds: ['unknown'] };
    const drafts = [createDataScopeRuleDraft(rule), createDataScopeRuleDraft(rule)];
    expect(drafts[0]!.localKey).not.toBe(drafts[1]!.localKey);
    const projected = projectDataScopeRules(drafts);
    expect(projected).toHaveLength(2);
    expect(JSON.stringify(projected)).not.toContain('localKey');
    expect(
      isDataScopeAssignmentDirty(
        { defaultDataScope: 'SELF', rules: [rule, rule] },
        { defaultDataScope: 'SELF', rules: projected },
      ),
    ).toBe(false);
  });

  it('资源默认的空 action 和缺失 action 视为重复，非法资源操作被拒绝', () => {
    expect(
      validateDataScopeRules(
        [
          { resourceType: 'purchase', scopeType: 'SELF', orgIds: [] },
          { resourceType: 'purchase', action: '', scopeType: 'ORG', orgIds: [] },
          { resourceType: 'missing', scopeType: 'ORG', orgIds: [] },
          { resourceType: 'purchase', action: 'INVALID', scopeType: 'ORG', orgIds: [] },
        ],
        { purchase: ['SAVE'] },
      ).map((error) => [error.index, error.field]),
    ).toEqual([
      [1, 'action'],
      [2, 'resourceType'],
      [3, 'action'],
    ]);
  });

  it('options 适配先搜索再分页，未知已选 ID 不会成为候选项', () => {
    const organizations = [
      { id: 'one', number: 'ONE', name: '一', namePath: '集团/一' },
      { id: 'two', number: 'TWO', name: '二', namePath: '集团/二' },
    ];
    expect(
      queryDataScopeOrganizations(organizations, { pageNum: 2, pageSize: 1, keyword: '集团' }),
    ).toEqual({ records: [organizations[1]], total: 2 });
    expect(
      queryDataScopeOrganizations(organizations, { pageNum: 1, pageSize: 20, keyword: ' one ' })
        .records,
    ).toEqual([organizations[0]]);
    expect(
      resolveDataScopeOrganizations(['unknown', 'two'], organizations).map(
        (organization) => organization.id,
      ),
    ).toEqual(['unknown', 'two']);
    expect(
      queryDataScopeOrganizations(organizations, { pageNum: 1, pageSize: 20, keyword: 'unknown' })
        .total,
    ).toBe(0);
  });
});
