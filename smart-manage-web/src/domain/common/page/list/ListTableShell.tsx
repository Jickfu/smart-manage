import type { ReactNode } from 'react';
import { Empty, Pagination, Splitter } from 'antd';
import './ListPage.css';

interface ListTableShellProps {
  table?: ReactNode;
  total?: number;
  selectedCount?: number;
  pageNum?: number;
  pageSize?: number;
  onPageChange?: (pageNum: number, pageSize: number) => void;
  showPagination?: boolean;
  /** 左侧树面板（左树右表布局） */
  treePanel?: ReactNode;
  /** 固定在表头可视区域右侧、不随表头横向滚动的操作。 */
  tableHeaderExtra?: ReactNode;
}

/**
 * 列表表格外壳 — 元信息栏 + 分页 + 表格主体。
 *
 * 选择模型：当前仅支持"当前页选择"。跨页全选待后续独立模型实现。
 */
const ListTableShell = ({
  table,
  total = 0,
  selectedCount = 0,
  pageNum = 1,
  pageSize = 20,
  onPageChange,
  showPagination = true,
  treePanel,
  tableHeaderExtra,
}: ListTableShellProps) => {
  const rightContent = (
    <div className="sm-list-table-content">
      {/* 元信息栏：总数 + 已选提示 + 分页 */}
      <div className="sm-list-table-meta">
        <div className="sm-list-table-count">
          <span>共 {total} 条</span>
          {selectedCount > 0 && <span>已选当前页 {selectedCount} 条</span>}
        </div>
        {showPagination && (
          <Pagination
            size="small"
            showSizeChanger
            pageSizeOptions={['10', '20', '50', '100']}
            current={pageNum}
            pageSize={pageSize}
            total={total}
            showTotal={(t) => `共 ${t} 条`}
            onChange={(nextPage, nextSize) => onPageChange?.(nextPage, nextSize)}
          />
        )}
      </div>
      {/* 表格主体 */}
      <div className="sm-list-table-body">
        {table ?? <Empty description="暂无列表配置" />}
        {tableHeaderExtra && <div className="sm-list-table-header-extra">{tableHeaderExtra}</div>}
      </div>
    </div>
  );

  return (
    <div className="sm-list-table-shell">
      {treePanel ? (
        <Splitter className="sm-list-table-split">
          <Splitter.Panel defaultSize={220} min={180} max="40%">
            <aside className="sm-list-tree-panel">{treePanel}</aside>
          </Splitter.Panel>
          <Splitter.Panel>{rightContent}</Splitter.Panel>
        </Splitter>
      ) : (
        rightContent
      )}
    </div>
  );
};

export default ListTableShell;
