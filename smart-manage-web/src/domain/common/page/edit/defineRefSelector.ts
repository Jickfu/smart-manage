import type { ReactNode } from 'react';
import type { RefSelectorFieldConfig } from './EditPage';
import type { RefSelectorFooterContext } from '@/domain/common/component/RefSelector';

type TypedRefSelectorFieldConfig<T extends object> = Omit<
  RefSelectorFieldConfig,
  'fetchFn' | 'displayRender' | 'columns' | 'footer' | 'onConfirm'
> & {
  footer?: (context: RefSelectorFooterContext<T>) => ReactNode;
  onConfirm?: (records: readonly T[]) => void | boolean | Promise<void | boolean>;
  fetchFn: (params: Parameters<RefSelectorFieldConfig['fetchFn']>[0]) => Promise<{
    records: T[];
    total: number;
  }>;
  displayRender: (record: T) => string;
  columns: {
    title: string;
    dataIndex: string;
    width?: number | string;
    render?: (text: unknown, record: T, index: number) => ReactNode;
  }[];
};

/**
 * 为业务页面保留完整记录泛型，在公共字段协议边界统一完成类型擦除。
 * 业务页面无需再通过双重断言连接 fetch、columns 与 displayRender。
 */
export function defineRefSelector<T extends object>(
  config: TypedRefSelectorFieldConfig<T>,
): RefSelectorFieldConfig {
  return {
    ...config,
    footer: config.footer
      ? (context) =>
          config.footer!({ ...context, selectedRecords: context.selectedRecords as readonly T[] })
      : undefined,
    onConfirm: config.onConfirm
      ? (records) => config.onConfirm!(records as readonly T[])
      : undefined,
    fetchFn: async (params) => {
      const result = await config.fetchFn(params);
      return { ...result, records: result.records as Record<string, unknown>[] };
    },
    displayRender: (record) => config.displayRender(record as T),
    columns: config.columns.map((column) => ({
      ...column,
      render: column.render
        ? (text, record, index) => column.render!(text, record as T, index)
        : undefined,
    })),
  };
}
