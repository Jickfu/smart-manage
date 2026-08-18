import { useState } from 'react';
import dayjs, { type Dayjs } from 'dayjs';
import { Button, Checkbox, DatePicker, Input } from 'antd';
import type {
  DateFilterOperator,
  ListFilterCondition,
  ListFilterOption,
  ListFilterOperator,
  ListFilterType,
  NumberFilterOperator,
  StringFilterOperator,
} from './listQuery';

interface ListColumnFilterProps {
  field: string;
  type: ListFilterType;
  options?: ListFilterOption[];
  value?: ListFilterCondition;
  onConfirm: (condition?: ListFilterCondition) => void;
}

const STRING_OPERATORS: Array<{ label: string; value: StringFilterOperator }> = [
  { label: '包含', value: 'CONTAINS' },
  { label: '不包含', value: 'NOT_CONTAINS' },
  { label: '等于', value: 'EQ' },
  { label: '不等于', value: 'NE' },
  { label: '以……开始', value: 'STARTS_WITH' },
  { label: '以……结束', value: 'ENDS_WITH' },
  { label: '为空', value: 'EMPTY' },
  { label: '不为空', value: 'NOT_EMPTY' },
];

const NUMBER_OPERATORS: Array<{ label: string; value: NumberFilterOperator }> = [
  { label: '等于', value: 'EQ' },
  { label: '不等于', value: 'NE' },
  { label: '大于', value: 'GT' },
  { label: '大于等于', value: 'GE' },
  { label: '小于', value: 'LT' },
  { label: '小于等于', value: 'LE' },
];

const DATE_OPERATORS: Array<{ label: string; value: DateFilterOperator }> = [
  { label: '今天', value: 'TODAY' },
  { label: '本周', value: 'THIS_WEEK' },
  { label: '本月', value: 'THIS_MONTH' },
  { label: '上月', value: 'LAST_MONTH' },
  { label: '过去一个月', value: 'PAST_MONTH' },
  { label: '过去三个月', value: 'PAST_THREE_MONTHS' },
  { label: '从…到…', value: 'BETWEEN' },
  { label: '等于', value: 'EQ' },
];

const defaultOperator = (type: ListFilterType): ListFilterOperator => {
  if (type === 'number') return 'EQ';
  if (type === 'date') return 'TODAY';
  if (type === 'enum' || type === 'boolean') return 'IN';
  return 'CONTAINS';
};

const isValuelessOperator = (operator: ListFilterOperator) =>
  operator === 'EMPTY' ||
  operator === 'NOT_EMPTY' ||
  ['TODAY', 'THIS_WEEK', 'THIS_MONTH', 'LAST_MONTH', 'PAST_MONTH', 'PAST_THREE_MONTHS'].includes(
    operator,
  );

const ListColumnFilter = ({
  field,
  type,
  options = [],
  value,
  onConfirm,
}: ListColumnFilterProps) => {
  const [operator, setOperator] = useState<ListFilterOperator>(
    value?.operator ?? defaultOperator(type),
  );
  const [singleValue, setSingleValue] = useState<ListFilterCondition['value']>(value?.value);
  const [multipleValues, setMultipleValues] = useState<Array<string | number | boolean>>(
    value?.values ?? [],
  );
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null] | null>(() =>
    value?.operator === 'BETWEEN' && value.values?.length === 2
      ? [dayjs(String(value.values[0])), dayjs(String(value.values[1]))]
      : null,
  );
  const [equalDate, setEqualDate] = useState<Dayjs | null>(() =>
    value?.operator === 'EQ' && value.value ? dayjs(String(value.value)) : null,
  );

  const apply = () => {
    if (type === 'enum' || type === 'boolean') {
      onConfirm(
        multipleValues.length > 0
          ? { field, type, operator: 'IN', values: multipleValues }
          : undefined,
      );
      return;
    }
    if (type === 'date' && operator === 'BETWEEN') {
      const values = dateRange?.filter(Boolean).map((date) => date?.format('YYYY-MM-DD')) ?? [];
      onConfirm(
        values.length === 2
          ? { field, type, operator, values: values.filter(Boolean) as string[] }
          : undefined,
      );
      return;
    }
    if (isValuelessOperator(operator)) {
      onConfirm({ field, type, operator });
      return;
    }
    const normalizedValue = typeof singleValue === 'string' ? singleValue.trim() : singleValue;
    onConfirm(
      normalizedValue !== undefined && normalizedValue !== ''
        ? { field, type, operator, value: normalizedValue }
        : undefined,
    );
  };

  const operatorOptions =
    type === 'string'
      ? STRING_OPERATORS
      : type === 'number'
        ? NUMBER_OPERATORS
        : type === 'date'
          ? DATE_OPERATORS
          : [];

  const resolvedOptions =
    type === 'boolean'
      ? [
          { label: '是', value: true },
          { label: '否', value: false },
        ]
      : options;

  return (
    <div className="sm-list-column-filter" onKeyDown={(event) => event.stopPropagation()}>
      {operatorOptions.length > 0 && (
        <div className="sm-list-column-filter-operators">
          {operatorOptions.map((item) => (
            <button
              key={item.value}
              type="button"
              className={
                operator === item.value ? 'sm-list-column-filter-operator-active' : undefined
              }
              onClick={() => {
                setOperator(item.value);
                setSingleValue(undefined);
                setDateRange(null);
                setEqualDate(null);
              }}
            >
              <span>{item.label}</span>
              {operator === item.value && <span>✓</span>}
            </button>
          ))}
        </div>
      )}

      {(type === 'enum' || type === 'boolean') && (
        <div className="sm-list-column-filter-options">
          <Checkbox
            checked={resolvedOptions.length > 0 && multipleValues.length === resolvedOptions.length}
            indeterminate={
              multipleValues.length > 0 && multipleValues.length < resolvedOptions.length
            }
            onChange={(event) =>
              setMultipleValues(
                event.target.checked ? resolvedOptions.map((item) => item.value) : [],
              )
            }
          >
            全选
          </Checkbox>
          {resolvedOptions.map((item) => (
            <Checkbox
              key={String(item.value)}
              checked={multipleValues.includes(item.value)}
              onChange={(event) =>
                setMultipleValues((current) =>
                  event.target.checked
                    ? [...current, item.value]
                    : current.filter((itemValue) => itemValue !== item.value),
                )
              }
            >
              {item.label}
            </Checkbox>
          ))}
        </div>
      )}

      {type === 'string' && !isValuelessOperator(operator) && (
        <div className="sm-list-column-filter-value">
          <Input
            autoFocus
            value={typeof singleValue === 'string' ? singleValue : ''}
            onChange={(event) => setSingleValue(event.target.value)}
            onPressEnter={apply}
          />
        </div>
      )}

      {type === 'number' && (
        <div className="sm-list-column-filter-value">
          <Input
            autoFocus
            inputMode="decimal"
            value={singleValue === undefined ? '' : String(singleValue)}
            onChange={(event) => setSingleValue(event.target.value)}
            onPressEnter={apply}
          />
        </div>
      )}

      {type === 'date' && operator === 'BETWEEN' && (
        <div className="sm-list-column-filter-value">
          <DatePicker.RangePicker
            value={dateRange}
            onChange={(nextValue) => setDateRange(nextValue)}
          />
        </div>
      )}

      {type === 'date' && operator === 'EQ' && (
        <div className="sm-list-column-filter-value">
          <DatePicker
            value={equalDate}
            onChange={(nextValue) => {
              setEqualDate(nextValue);
              setSingleValue(nextValue?.format('YYYY-MM-DD'));
            }}
          />
        </div>
      )}

      <div className="sm-list-column-filter-footer">
        <Button
          onClick={() => {
            setSingleValue(undefined);
            setMultipleValues([]);
            setDateRange(null);
            setEqualDate(null);
            onConfirm(undefined);
          }}
        >
          重置
        </Button>
        <Button type="primary" onClick={apply}>
          确定
        </Button>
      </div>
    </div>
  );
};

export default ListColumnFilter;
