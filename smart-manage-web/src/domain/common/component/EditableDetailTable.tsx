import type { Key, ReactNode } from 'react';
import { Table } from 'antd';
import type { TableProps } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import './EditableDetailTable.css';

interface EditableDetailTableProps<RecordType extends object> extends Omit<
  TableProps<RecordType>,
  'pagination' | 'rowSelection' | 'size'
> {
  editable: boolean;
  /** 是否显示默认 # 序号列；已有业务顺序列时关闭。 */
  showIndexColumn?: boolean;
  selectedRowKeys?: Key[];
  onSelectedRowKeysChange?: (keys: Key[]) => void;
}

/** 编辑明细表格的统一视觉与选择骨架，业务列和领域操作由使用方维护。 */
export function EditableDetailTable<RecordType extends object>({
  editable,
  showIndexColumn = true,
  selectedRowKeys = [],
  onSelectedRowKeysChange,
  className,
  columns,
  onRow,
  rowKey = 'key',
  scroll,
  ...tableProps
}: EditableDetailTableProps<RecordType>) {
  const mergedColumns: ColumnsType<RecordType> = [
    ...(showIndexColumn
      ? [
          {
            key: '__index',
            title: '#',
            width: 44,
            className: 'sm-editable-detail-table-index-column',
            align: 'center' as const,
            fixed: 'left' as const,
            render: (_value: unknown, _record: RecordType, index: number) => index + 1,
          },
        ]
      : []),
    ...(columns ?? []),
  ];

  const resolveRowKey = (record: RecordType): Key | undefined => {
    const resolvedKey =
      typeof rowKey === 'function'
        ? rowKey(record)
        : (record as Record<string, unknown>)[String(rowKey)];
    return typeof resolvedKey === 'string' || typeof resolvedKey === 'number'
      ? resolvedKey
      : undefined;
  };

  return (
    <div className="sm-editable-detail-table">
      <Table<RecordType>
        {...tableProps}
        className={className}
        columns={mergedColumns}
        rowKey={rowKey}
        pagination={false}
        size="small"
        scroll={{ x: 'max-content', ...scroll }}
        rowSelection={
          editable && onSelectedRowKeysChange
            ? {
                selectedRowKeys,
                onChange: onSelectedRowKeysChange,
                columnWidth: 36,
                fixed: true,
              }
            : undefined
        }
        onRow={(record, index) => {
          const configuredRow = onRow?.(record, index) ?? {};
          const configuredOnClick = configuredRow.onClick;
          return {
            ...configuredRow,
            onClick: (event) => {
              configuredOnClick?.(event);
              if (event.defaultPrevented || !editable || !onSelectedRowKeysChange) return;
              const target = event.target as HTMLElement;
              // 复选框维持增量多选；点击行内其他区域则收敛为当前行单选。
              if (target.closest('.ant-table-selection-column')) return;
              const currentRowKey = resolveRowKey(record);
              if (currentRowKey !== undefined) onSelectedRowKeysChange([currentRowKey]);
            },
          };
        }}
      />
    </div>
  );
}

/** 必填标识统一放在列头，避免每个单元格重复占用空间。 */
export function RequiredDetailColumnTitle({ children }: { children: ReactNode }) {
  return <span className="sm-editable-detail-table-required">{children}</span>;
}
