import type { ReactNode } from 'react';
import './FormFieldLayout.css';

interface FormFieldGridProps {
  children: ReactNode;
  /** 限制字段容器的最大列数；两列和单列容器会作为整体居中。 */
  maxColumns?: 1 | 2 | 4;
}

interface FormFieldCellProps {
  children: ReactNode;
  /** 占用两个或三个标准字段列。 */
  columnSpan?: 2 | 3;
  /** 延伸至第四列标准控件的右边缘。 */
  fullWidth?: boolean;
}

/**
 * 通用字段容器。
 *
 * 仅封装已经调优的 sm-edit-fields 布局结构；字段换列和宽度计算仍由
 * EditPage.css 中的既有容器查询负责。
 */
export function FormFieldGrid({ children, maxColumns = 4 }: FormFieldGridProps) {
  const className = [
    'sm-edit-fields',
    maxColumns === 2 ? 'sm-edit-fields--max-2' : '',
    maxColumns === 1 ? 'sm-edit-fields--max-1' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return <div className={className}>{children}</div>;
}

/**
 * 通用字段占位单元。
 *
 * 必须让 Form.Item 作为直接子元素，以保持跨列字段的 +230px 宽度计算不变。
 */
export function FormFieldCell({ children, columnSpan, fullWidth = false }: FormFieldCellProps) {
  const className = [
    'sm-edit-field',
    columnSpan === 2 ? 'sm-edit-field--span-2' : '',
    columnSpan === 3 ? 'sm-edit-field--span-3' : '',
    fullWidth ? 'sm-edit-field--full' : '',
  ]
    .filter(Boolean)
    .join(' ');

  return <div className={className}>{children}</div>;
}
