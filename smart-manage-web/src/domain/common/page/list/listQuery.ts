import type { ReactNode } from 'react';

export type ListFilterType = 'string' | 'number' | 'enum' | 'date' | 'boolean';

export type StringFilterOperator =
  | 'CONTAINS'
  | 'NOT_CONTAINS'
  | 'EQ'
  | 'NE'
  | 'STARTS_WITH'
  | 'ENDS_WITH'
  | 'EMPTY'
  | 'NOT_EMPTY';

export type NumberFilterOperator = 'EQ' | 'NE' | 'GT' | 'GE' | 'LT' | 'LE';

export type DateFilterOperator =
  | 'TODAY'
  | 'THIS_WEEK'
  | 'THIS_MONTH'
  | 'LAST_MONTH'
  | 'PAST_MONTH'
  | 'PAST_THREE_MONTHS'
  | 'BETWEEN'
  | 'EQ';

export type ListFilterOperator =
  | StringFilterOperator
  | NumberFilterOperator
  | DateFilterOperator
  | 'IN';

export interface ListFilterCondition {
  field: string;
  type: ListFilterType;
  operator: ListFilterOperator;
  value?: string | number | boolean;
  values?: Array<string | number | boolean>;
}

export interface ListSortCondition {
  field: string;
  order: 'ASC' | 'DESC';
}

export interface ListFilterOption {
  label: ReactNode;
  value: string | number | boolean;
}

export interface ListColumnFeature {
  label: string;
  filter?: {
    type: ListFilterType;
    options?: ListFilterOption[];
  };
  sorter?: boolean;
}

export type ListColumnFeatures = Record<string, ListColumnFeature>;

const DEFAULT_BOOLEAN_FILTER_OPTIONS: ListFilterOption[] = [
  { label: '是', value: true },
  { label: '否', value: false },
];

/** 统一解析枚举和布尔筛选选项；布尔字段可按业务语义覆盖默认“是/否”。 */
export const resolveListFilterOptions = (
  type: ListFilterType,
  options?: ListFilterOption[],
): ListFilterOption[] =>
  type === 'boolean' ? (options ?? DEFAULT_BOOLEAN_FILTER_OPTIONS) : (options ?? []);

export const serializeListFilters = (filters: ListFilterCondition[]) =>
  filters.length > 0 ? JSON.stringify(filters) : undefined;
