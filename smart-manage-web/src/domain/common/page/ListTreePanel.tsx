import type { ReactNode } from 'react';

interface ListTreePanelProps {
  /** 固定在树上方的任意控件，例如搜索框、按钮组或组合工具栏。 */
  header?: ReactNode;
  /** 独立支持横向和纵向滚动的树主体。 */
  children: ReactNode;
  /** 固定在树下方的任意控件，例如范围或状态筛选。 */
  footer?: ReactNode;
}

/** 左树通用三段式布局：可选头部 + 独立滚动主体 + 可选底部。 */
const ListTreePanel = ({ header, children, footer }: ListTreePanelProps) => (
  <div className="sm-list-tree-layout">
    {header && <div className="sm-list-tree-header">{header}</div>}
    <div className="sm-list-tree-content">{children}</div>
    {footer && <div className="sm-list-tree-footer">{footer}</div>}
  </div>
);

export default ListTreePanel;
