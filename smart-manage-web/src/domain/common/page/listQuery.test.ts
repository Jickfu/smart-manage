import { describe, expect, it } from 'vitest';
import { serializeListFilters, type ListFilterCondition } from './listQuery';

describe('listQuery', () => {
  it('空条件不向后端发送冗余 JSON', () => {
    expect(serializeListFilters([])).toBeUndefined();
  });

  it('完整保留字段类型、操作符和值', () => {
    const filters: ListFilterCondition[] = [
      { field: 'name', type: 'string', operator: 'CONTAINS', value: '采购' },
      { field: 'enabled', type: 'boolean', operator: 'IN', values: [true] },
      {
        field: 'createTime',
        type: 'date',
        operator: 'BETWEEN',
        values: ['2026-08-01', '2026-08-18'],
      },
    ];

    expect(JSON.parse(serializeListFilters(filters)!)).toEqual(filters);
  });
});
