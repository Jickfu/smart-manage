import { describe, expect, it, vi } from 'vitest';
import dayjs from 'dayjs';
import { DATE_TIME_FORMAT, beijingNow } from '../../component/beijingTime';
import {
  getDatePickerValueProps,
  getDateTimePickerValueProps,
  normalizeDatePickerValue,
  normalizeDateTimePickerValue,
} from './dateFormValue';

describe('dateFormValue', () => {
  it('将后端日期字符串转换为 DatePicker 值', () => {
    expect(getDatePickerValueProps('2026-07-28').value?.format('YYYY-MM-DD')).toBe('2026-07-28');
    expect(getDatePickerValueProps('').value).toBeUndefined();
  });

  it('北京时间跨日取今天，且浏览器夏令时不改写表单钟表时间', () => {
    vi.useFakeTimers();
    try {
      vi.setSystemTime(new Date('2026-12-31T16:30:00Z'));
      expect(beijingNow().format(DATE_TIME_FORMAT)).toBe('2027-01-01 00:30:00');
      const { value } = getDateTimePickerValueProps('2026-03-08 02:30:00');
      expect(normalizeDateTimePickerValue(value)).toBe('2026-03-08 02:30:00');
    } finally {
      vi.useRealTimers();
    }
  });

  it('将 DatePicker 值转换为后端日期字符串', () => {
    expect(normalizeDatePickerValue(dayjs('2026-07-29'))).toBe('2026-07-29');
    expect(normalizeDatePickerValue(null)).toBeUndefined();
  });
});
