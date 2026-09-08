import { DatePicker, Select } from 'antd';
import { ListFilterFields, ListFilterField } from '@/domain/common/page/list/ListFilterFields';
import type { AuditLogFilters } from './types';

interface Props<TFilters extends AuditLogFilters> {
  values: TFilters;
  eventTypeOptions?: Array<{ label: string; value: string }>;
  onFilter: (values: TFilters) => void;
}

export default function AuditLogFilter<TFilters extends AuditLogFilters>({
  values,
  eventTypeOptions,
  onFilter,
}: Props<TFilters>) {
  return (
    <ListFilterFields>
      <ListFilterField label="结果">
        <Select
          allowClear
          placeholder="全部"
          value={values.success}
          options={[
            { label: '成功', value: true },
            { label: '失败', value: false },
          ]}
          onChange={(success) => onFilter({ ...values, success })}
        />
      </ListFilterField>
      {eventTypeOptions && (
        <ListFilterField label="事件">
          <Select
            allowClear
            placeholder="全部"
            value={values.eventType}
            options={eventTypeOptions}
            onChange={(eventType) => onFilter({ ...values, eventType })}
          />
        </ListFilterField>
      )}
      <ListFilterField label="发生时间">
        <DatePicker.RangePicker
          showTime
          value={values.timeRange ?? null}
          onChange={(dates) =>
            onFilter({
              ...values,
              timeRange: dates?.[0] && dates[1] ? [dates[0], dates[1]] : undefined,
            })
          }
        />
      </ListFilterField>
    </ListFilterFields>
  );
}
