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
  scroll,
  ...tableProps
}: EditableDetailTableProps<RecordType>) {
  const mergedColumns: ColumnsType<RecordType> = [
    ...(showIndexColumn
      ? [
          {
            key: '__index',
            title: '#',
            width: 56,
            align: 'center' as const,
            render: (_value: unknown, _record: RecordType, index: number) => index + 1,
          },
        ]
      : []),
    ...(columns ?? []),
  ];

  return (
    <div className="sm-editable-detail-table">
      <Table<RecordType>
        {...tableProps}
        className={className}
        columns={mergedColumns}
        pagination={false}
        size="small"
        scroll={{ x: 'max-content', ...scroll }}
        rowSelection={
          editable && onSelectedRowKeysChange
            ? {
                selectedRowKeys,
                onChange: onSelectedRowKeysChange,
              }
            : undefined
        }
      />
    </div>
  );
}

/** 必填标识统一放在列头，避免每个单元格重复占用空间。 */
export function RequiredDetailColumnTitle({ children }: { children: ReactNode }) {
  return <span className="sm-editable-detail-table-required">{children}</span>;
}
