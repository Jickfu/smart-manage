import { describe, expect, it } from 'vitest';
import {
  createBillStatusCatalog,
  isStandardBillEditable,
  StandardBillStatus,
  standardBillStatusCatalog,
} from './billStatus';

describe('bill status catalog', () => {
  it('keeps the platform A-D contract stable', () => {
    expect(standardBillStatusCatalog.options).toEqual([
      { value: 'A', label: '暂存' },
      { value: 'B', label: '已提交' },
      { value: 'C', label: '已审核' },
      { value: 'D', label: '已关闭' },
    ]);
    expect(isStandardBillEditable(StandardBillStatus.SAVED)).toBe(true);
    expect(isStandardBillEditable(StandardBillStatus.SUBMITTED)).toBe(false);
    expect(isStandardBillEditable(undefined)).toBe(false);
  });

  it('allows aggregate extensions but rejects collisions and invalid codes', () => {
    const catalog = createBillStatusCatalog([{ value: 'E', label: ' 已下达 ', color: 'cyan' }]);
    expect(catalog.require('E').label).toBe('已下达');
    expect(() => createBillStatusCatalog([{ value: 'A', label: '覆盖', color: 'red' }])).toThrow(
      '单据状态编码重复',
    );
    expect(() => createBillStatusCatalog([{ value: 'AA', label: '非法', color: 'red' }])).toThrow(
      '单据状态编码必须是一个大写 ASCII 字母',
    );
  });
});
