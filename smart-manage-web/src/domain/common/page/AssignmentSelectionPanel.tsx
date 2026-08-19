import type { ReactNode } from 'react';
import { Checkbox, Input, Splitter } from 'antd';
import './AssignmentPage.css';

interface AssignmentSelectionPanelProps {
  title: string;
  keyword: string;
  keywordPlaceholder: string;
  onlySelected: boolean;
  meta: ReactNode;
  actions?: ReactNode;
  treePanel?: ReactNode;
  children: ReactNode;
  onKeywordChange: (keyword: string) => void;
  onOnlySelectedChange: (onlySelected: boolean) => void;
}

const AssignmentTableContent = ({
  meta,
  actions,
  children,
}: Pick<AssignmentSelectionPanelProps, 'meta' | 'actions' | 'children'>) => (
  <div className="sm-assignment-table-content">
    <div className="sm-assignment-table-meta">
      <div>{meta}</div>
      {actions && <div className="sm-assignment-table-actions">{actions}</div>}
    </div>
    <div className="sm-assignment-table-body">{children}</div>
  </div>
);

/** 关系分配内容壳层：统一搜索、已选筛选、左树右表和固定高度滚动链。 */
export function AssignmentSelectionPanel({
  title,
  keyword,
  keywordPlaceholder,
  onlySelected,
  meta,
  actions,
  treePanel,
  children,
  onKeywordChange,
  onOnlySelectedChange,
}: AssignmentSelectionPanelProps) {
  const tableContent = (
    <AssignmentTableContent meta={meta} actions={actions}>
      {children}
    </AssignmentTableContent>
  );

  return (
    <section className="sm-assignment-selection-panel">
      <div className="sm-assignment-selection-header">
        <h2>{title}</h2>
        <div className="sm-assignment-selection-filters">
          <Checkbox
            checked={onlySelected}
            onChange={(event) => onOnlySelectedChange(event.target.checked)}
          >
            仅看已选
          </Checkbox>
          <Input.Search
            allowClear
            value={keyword}
            placeholder={keywordPlaceholder}
            onChange={(event) => onKeywordChange(event.target.value)}
          />
        </div>
      </div>
      <div className="sm-assignment-selection-body">
        {treePanel ? (
          <Splitter className="sm-assignment-selection-split">
            <Splitter.Panel defaultSize={240} min={200} max="40%">
              <aside className="sm-assignment-tree-panel">{treePanel}</aside>
            </Splitter.Panel>
            <Splitter.Panel>{tableContent}</Splitter.Panel>
          </Splitter>
        ) : (
          tableContent
        )}
      </div>
    </section>
  );
}
