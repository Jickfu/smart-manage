import { describe, expect, it } from 'vitest';
import { createFilterSummaryLabel } from './useListColumnFeatures';

describe('createFilterSummaryLabel', () => {
  it('使用选项展示值生成筛选摘要', () => {
    expect(
      createFilterSummaryLabel(
        { field: 'status', type: 'enum', operator: 'IN', values: ['ENABLED'] },
        {
          label: '状态',
          filter: { type: 'enum', options: [{ label: '启用', value: 'ENABLED' }] },
        },
      ),
    ).toBe('状态：是 启用');
  });

  it('未配置字段标签时使用字段名', () => {
    expect(
      createFilterSummaryLabel(
        { field: 'custom', type: 'number', operator: 'EQ', value: 1 },
        undefined,
      ),
    ).toBe('custom：等于 1');
  });
});
