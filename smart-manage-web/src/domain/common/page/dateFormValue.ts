import dayjs from 'dayjs';
import type { Dayjs } from 'dayjs';

const DATE_FORMAT = 'YYYY-MM-DD';

/** 后端日期字符串转换为 Ant Design DatePicker 使用的 Dayjs 值。 */
export function getDatePickerValueProps(value?: unknown): { value?: Dayjs } {
  return { value: value ? dayjs(String(value), DATE_FORMAT) : undefined };
}

/** DatePicker 值统一转换为后端接口约定的日期字符串。 */
export function normalizeDatePickerValue(value?: Dayjs | null): string | undefined {
  return value?.format(DATE_FORMAT);
}
