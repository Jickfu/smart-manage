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

export const serializeListFilters = (filters: ListFilterCondition[]) =>
  filters.length > 0 ? JSON.stringify(filters) : undefined;
