import { useMemo } from 'react';
import ListTree from '@/domain/common/page/list/ListTree';
import ListTreePanel from '@/domain/common/page/list/ListTreePanel';
import type { SchedulerCatalogNode } from './schedulerScope';

interface SchedulerScopeTreeProps {
  title: string;
  nodes?: SchedulerCatalogNode[];
  selectedKey: string;
  onSelect: (key: string) => void;
}

const EMPTY_NODES: SchedulerCatalogNode[] = [];

/** 任务和执行记录共用领域、应用两层导航，目录数据由各自接口提供。 */
export function SchedulerScopeTree({
  title,
  nodes = EMPTY_NODES,
  selectedKey,
  onSelect,
}: SchedulerScopeTreeProps) {
  const treeData = useMemo(() => [{ key: 'all', title, children: nodes }], [nodes, title]);
  return (
    <ListTreePanel>
      <ListTree
        virtual={false}
        showLine={false}
        blockNode
        treeData={treeData}
        selectedKeys={[selectedKey]}
        defaultExpandedKeys={['all', ...nodes.map((node) => node.key)]}
        onSelect={(keys) => onSelect(String(keys[0] ?? 'all'))}
      />
    </ListTreePanel>
  );
}
