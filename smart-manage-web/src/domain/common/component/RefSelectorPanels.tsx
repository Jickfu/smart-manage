import { Button, Empty } from 'antd';
import { CloseOutlined } from '@ant-design/icons';
import type { DataNode } from 'antd/es/tree';
import ListTree from '@/domain/common/page/list/ListTree';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';

interface SelectedPanelProps<T extends Record<string, unknown>> {
  selection: Map<string, T>;
  keyField: string;
  displayRender: (record: T) => string;
  onClear: () => void;
  onRemove: (key: string) => void;
}

export function RefSelectorSelectedPanel<T extends Record<string, unknown>>({
  selection,
  keyField,
  displayRender,
  onClear,
  onRemove,
}: SelectedPanelProps<T>) {
  return (
    <aside className="sm-ref-selector-selected-panel">
      <div className="sm-ref-selector-selected-header">
        <span>已选 {selection.size} 项</span>
        {selection.size > 0 && (
          <Button type="link" onClick={onClear}>
            清空
          </Button>
        )}
      </div>
      <div className="sm-ref-selector-selected-list">
        {[...selection.values()].map((record) => {
          const key = String(record[keyField]);
          return (
            <div key={key} className="sm-ref-selector-selected-item">
              <span className="sm-ref-selector-selected-item-label">{displayRender(record)}</span>
              <span className="sm-ref-selector-selected-item-remove" onClick={() => onRemove(key)}>
                <CloseOutlined />
              </span>
            </div>
          );
        })}
        {selection.size === 0 && (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="暂未选择"
            className="sm-ref-selector-empty"
          />
        )}
      </div>
    </aside>
  );
}

interface TreePanelProps {
  treeData?: Record<string, unknown>[];
  fieldNames?: { key: string; title: string; children: string };
  footer?: React.ReactNode;
  selectedKey?: string;
  onSelect: (key?: string) => void;
}

export function RefSelectorTreePanel({
  treeData,
  fieldNames,
  footer,
  selectedKey,
  onSelect,
}: TreePanelProps) {
  if (!treeData?.length) return null;
  return (
    <ListTreePanel footer={footer}>
      <ListTree
        treeData={treeData as unknown as DataNode[]}
        fieldNames={fieldNames}
        onSelect={(keys) => onSelect(keys.length > 0 ? String(keys[0]) : undefined)}
        selectedKeys={selectedKey ? [selectedKey] : []}
        defaultExpandAll
        blockNode
      />
    </ListTreePanel>
  );
}
