import { describe, expect, it } from 'vitest';
import type { ColumnsType } from 'antd/es/table/interface';
import {
  applyColumnSettings,
  createDefaultColumnSettings,
  mergeColumnSettings,
  moveColumnSetting,
  normalizeFixedColumnOrder,
  validateColumnSettings,
} from './columnSettings';

interface RecordRow {
  id: string;
  name: string;
}

const columns: ColumnsType<RecordRow> = [
  { key: 'id', title: '编码', dataIndex: 'id', width: 120 },
  { key: 'name', title: '名称', dataIndex: 'name' },
];

describe('columnSettings', () => {
  it('从列定义创建默认配置，并保留自动宽度列', () => {
    expect(createDefaultColumnSettings(columns)).toEqual([
      expect.objectContaining({ key: 'id', widthMode: 'fixed', width: 120 }),
      expect.objectContaining({ key: 'name', widthMode: 'auto', width: undefined }),
    ]);
  });

  it('合并配置时丢弃已删除列并补充新增列', () => {
    const defaults = createDefaultColumnSettings(columns);
    const stored = [
      { ...defaults[1]!, hidden: true },
      { ...defaults[0]!, key: 'deleted' },
    ];
    expect(
      mergeColumnSettings(defaults, stored).map(({ key, hidden }) => ({ key, hidden })),
    ).toEqual([
      { key: 'name', hidden: true },
      { key: 'id', hidden: false },
    ]);
  });

  it('应用顺序、对齐、冻结、显隐和自动宽度', () => {
    const settings = createDefaultColumnSettings(columns);
    settings[0] = { ...settings[0]!, fixed: 'right', align: 'center' };
    settings[1] = { ...settings[1]!, hidden: true };
    const applied = applyColumnSettings(columns, settings);
    expect(applied.map((column) => column.key)).toEqual(['name', 'id']);
    expect(applied[0]).toMatchObject({ hidden: true });
    expect(applied[0]!.width).toBeUndefined();
    expect(applied[1]).toMatchObject({ fixed: 'right', align: 'center', width: 120 });
  });

  it('冻结列按左、普通、右排序，按钮只在同组内移动', () => {
    const defaults = createDefaultColumnSettings(columns);
    const settings = [
      { ...defaults[0]!, fixed: 'right' as const },
      { ...defaults[1]!, fixed: 'none' as const },
    ];
    expect(normalizeFixedColumnOrder(settings).map((setting) => setting.key)).toEqual([
      'name',
      'id',
    ]);
    expect(moveColumnSetting(settings, 'id', 'top')).toEqual(settings);
  });

  it('要求至少一个可见、未冻结的自动宽度列', () => {
    const settings = createDefaultColumnSettings(columns);
    expect(validateColumnSettings(settings)).toBeUndefined();
    expect(
      validateColumnSettings(
        settings.map((setting) => ({
          ...setting,
          widthMode: 'fixed',
          width: setting.width ?? 120,
        })),
      ),
    ).toContain('自动宽度列');
  });

  it('固定宽度必须处于允许范围', () => {
    const settings = createDefaultColumnSettings(columns);
    settings[0] = { ...settings[0]!, width: undefined };
    expect(validateColumnSettings(settings)).toContain('40–600px');
  });
});
