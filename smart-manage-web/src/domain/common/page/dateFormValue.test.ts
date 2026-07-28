import { describe, expect, it } from 'vitest';
import dayjs from 'dayjs';
import { getDatePickerValueProps, normalizeDatePickerValue } from './dateFormValue';

describe('dateFormValue', () => {
  it('将后端日期字符串转换为 DatePicker 值', () => {
    expect(getDatePickerValueProps('2026-07-28').value?.format('YYYY-MM-DD')).toBe('2026-07-28');
    expect(getDatePickerValueProps('').value).toBeUndefined();
  });

  it('将 DatePicker 值转换为后端日期字符串', () => {
    expect(normalizeDatePickerValue(dayjs('2026-07-29'))).toBe('2026-07-29');
    expect(normalizeDatePickerValue(null)).toBeUndefined();
  });
});
