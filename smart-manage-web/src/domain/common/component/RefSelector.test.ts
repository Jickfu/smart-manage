import { describe, expect, it } from 'vitest';
import { formatRefSelectorDisplayText, isRefSelectorTextOverflowing } from './refSelectorDisplay';

interface ReferenceValue {
  name: string;
}

const displayReference = (record: ReferenceValue) => record.name;

describe('RefSelector 触发器显示', () => {
  it('使用中文逗号拼接多选显示值', () => {
    expect(
      formatRefSelectorDisplayText(
        [{ name: '选项1' }, { name: '选项2' }, { name: '选项3' }],
        displayReference,
      ),
    ).toBe('选项1，选项2，选项3');
  });

  it('空值显示为空字符串，单选显示完整文本', () => {
    expect(formatRefSelectorDisplayText(null, displayReference)).toBe('');
    expect(formatRefSelectorDisplayText([], displayReference)).toBe('');
    expect(formatRefSelectorDisplayText({ name: '选项1' }, displayReference)).toBe('选项1');
  });

  it('只在文本超过未显示总数时的可用宽度后判定溢出', () => {
    expect(isRefSelectorTextOverflowing(203, 200)).toBe(true);
    expect(isRefSelectorTextOverflowing(202, 200)).toBe(false);
    expect(isRefSelectorTextOverflowing(203, 160, 40)).toBe(true);
    expect(isRefSelectorTextOverflowing(202, 160, 40)).toBe(false);
  });
});
