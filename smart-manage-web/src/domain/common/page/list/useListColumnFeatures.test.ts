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
    ).toBe('状态：启用');
  });

  it('布尔筛选使用业务选项且不显示 IN 操作词', () => {
    expect(
      createFilterSummaryLabel(
        { field: 'enabled', type: 'boolean', operator: 'IN', values: [true, false] },
        {
          label: '状态',
          filter: {
            type: 'boolean',
            options: [
              { label: '启用', value: true },
              { label: '禁用', value: false },
            ],
          },
        },
      ),
    ).toBe('状态：启用、禁用');
  });

  it('未配置业务选项的布尔筛选使用是和否而不是原始值', () => {
    expect(
      createFilterSummaryLabel(
        { field: 'leaf', type: 'boolean', operator: 'IN', values: [true, false] },
        { label: '叶子节点', filter: { type: 'boolean' } },
      ),
    ).toBe('叶子节点：是、否');
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
