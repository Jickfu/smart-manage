import { useMemo } from 'react';
import type { ColumnsType, ColumnType, FilterDropdownProps } from 'antd/es/table/interface';
import { FilterOutlined } from '@ant-design/icons';
import ListColumnFilter from './ListColumnFilter';
import {
  resolveListFilterOptions,
  type ListColumnFeatures,
  type ListFilterCondition,
  type ListSortCondition,
} from './listQuery';

export const createFilterSummaryLabel = (
  condition: ListFilterCondition,
  feature: ListColumnFeatures[string] | undefined,
) => {
  const operatorLabels: Record<string, string> = {
    CONTAINS: '包含',
    NOT_CONTAINS: '不包含',
    EQ: '等于',
    NE: '不等于',
    STARTS_WITH: '开头是',
    ENDS_WITH: '结尾是',
    EMPTY: '为空',
    NOT_EMPTY: '不为空',
    GT: '大于',
    GE: '大于等于',
    LT: '小于',
    LE: '小于等于',
    TODAY: '今天',
    THIS_WEEK: '本周',
    THIS_MONTH: '本月',
    LAST_MONTH: '上月',
    PAST_MONTH: '过去一个月',
    PAST_THREE_MONTHS: '过去三个月',
    BETWEEN: '从…到…',
  };
  const values = condition.values ?? (condition.value == null ? [] : [condition.value]);
  const options = feature?.filter
    ? resolveListFilterOptions(feature.filter.type, feature.filter.options)
    : [];
  const displayValues = values.map((value) => {
    const option = options.find((item) => item.value === value);
    return typeof option?.label === 'string' ? option.label : String(value);
  });
  const fieldLabel = feature?.label ?? condition.field;
  if (condition.operator === 'IN') return `${fieldLabel}：${displayValues.join('、')}`;
  const suffix = displayValues.length ? ` ${displayValues.join('、')}` : '';
  return `${fieldLabel}：${operatorLabels[condition.operator] ?? condition.operator}${suffix}`;
};

interface Options<T> {
  columns: ColumnsType<T>;
  features?: ListColumnFeatures;
  filters: ListFilterCondition[];
  sort?: ListSortCondition;
  onFiltersChange?: (filters: ListFilterCondition[]) => void;
}

export function useListColumnFeatures<T>({
  columns,
  features,
  filters,
  sort,
  onFiltersChange,
}: Options<T>) {
  return useMemo(
    () =>
      columns.map((column) => {
        if ('children' in column) return column;
        const typedColumn = column as ColumnType<T>;
        const dataIndex = typedColumn.dataIndex;
        const columnKey = String(
          typedColumn.key ?? (Array.isArray(dataIndex) ? dataIndex.join('.') : (dataIndex ?? '')),
        );
        const feature = features?.[columnKey];
        if (!feature) return typedColumn;
        const filter = filters.find((item) => item.field === columnKey);
        return {
          ...typedColumn,
          key: typedColumn.key ?? columnKey,
          ...(feature.filter
            ? {
                filteredValue: filter ? [JSON.stringify(filter)] : null,
                filterIcon: (filtered: boolean) => (
                  <FilterOutlined className={filtered ? 'sm-list-filter-icon-active' : undefined} />
                ),
                filterDropdown: ({ confirm }: FilterDropdownProps) => (
                  <ListColumnFilter
                    key={`${columnKey}-${JSON.stringify(filter ?? null)}`}
                    field={columnKey}
                    type={feature.filter?.type ?? 'string'}
                    options={feature.filter?.options}
                    value={filter}
                    onConfirm={(condition) => {
                      const nextFilters = filters.filter((item) => item.field !== columnKey);
                      if (condition) nextFilters.push(condition);
                      onFiltersChange?.(nextFilters);
                      confirm({ closeDropdown: true });
                    }}
                  />
                ),
              }
            : {}),
          ...(feature.sorter
            ? {
                sorter: true,
                sortOrder:
                  sort?.field === columnKey
                    ? sort.order === 'ASC'
                      ? ('ascend' as const)
                      : ('descend' as const)
                    : null,
              }
            : {}),
        };
      }) as ColumnsType<T>,
    [columns, features, filters, onFiltersChange, sort],
  );
}
