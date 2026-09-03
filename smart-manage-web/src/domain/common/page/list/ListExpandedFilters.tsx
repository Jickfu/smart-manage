import { DatePicker, Input, Select } from 'antd';
import dayjs from 'dayjs';
import type { ListColumnFeatures, ListFilterCondition } from './listQuery';

interface Props {
  features: ListColumnFeatures;
  filters: ListFilterCondition[];
  onChange: (filters: ListFilterCondition[]) => void;
}

/** 列表展开区的预置业务条件；布局固定为每行三项。 */
export default function ListExpandedFilters({ features, filters, onChange }: Props) {
  const filterFeatures = Object.entries(features).filter(([, feature]) => feature.filter);
  const update = (field: string, condition?: ListFilterCondition) => {
    const others = filters.filter((item) => item.field !== field);
    onChange(condition ? [...others, condition] : others);
  };

  return (
    <div className="sm-list-expanded-filter-grid">
      {filterFeatures.map(([field, feature]) => {
        const config = feature.filter!;
        const applied = filters.find((item) => item.field === field);
        const controlKey = `${field}-${JSON.stringify(applied ?? null)}`;
        let control: React.ReactNode;
        if (config.type === 'enum' || config.type === 'boolean') {
          const options =
            config.type === 'boolean'
              ? [
                  { label: '是', value: true },
                  { label: '否', value: false },
                ]
              : config.options;
          control = (
            <Select
              mode="multiple"
              allowClear
              maxTagCount="responsive"
              placeholder={`请选择${feature.label}`}
              value={applied?.values}
              options={options}
              onChange={(values) =>
                update(
                  field,
                  values.length ? { field, type: config.type, operator: 'IN', values } : undefined,
                )
              }
            />
          );
        } else if (config.type === 'date') {
          const range = applied?.operator === 'BETWEEN' ? applied.values?.map(String) : undefined;
          control = (
            <DatePicker.RangePicker
              allowClear
              value={range?.length === 2 ? [dayjs(range[0]), dayjs(range[1])] : null}
              onChange={(dates) =>
                update(
                  field,
                  dates?.[0] && dates[1]
                    ? {
                        field,
                        type: 'date',
                        operator: 'BETWEEN',
                        values: [dates[0].format('YYYY-MM-DD'), dates[1].format('YYYY-MM-DD')],
                      }
                    : undefined,
                )
              }
            />
          );
        } else {
          control = (
            <Input
              key={controlKey}
              allowClear
              inputMode={config.type === 'number' ? 'decimal' : undefined}
              defaultValue={applied?.value == null ? '' : String(applied.value)}
              placeholder={`请输入${feature.label}`}
              onBlur={(event) => {
                const value = event.target.value.trim();
                update(
                  field,
                  value
                    ? {
                        field,
                        type: config.type,
                        operator: config.type === 'number' ? 'EQ' : 'CONTAINS',
                        value,
                      }
                    : undefined,
                );
              }}
              onPressEnter={(event) => event.currentTarget.blur()}
            />
          );
        }
        return (
          <label className="sm-list-expanded-filter-item" key={field}>
            <span>{feature.label}</span>
            {control}
          </label>
        );
      })}
    </div>
  );
}
