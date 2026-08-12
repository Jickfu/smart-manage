import { useState } from 'react';
import { Tree } from 'antd';
import type { TreeProps } from 'antd';
import type { DataNode } from 'antd/es/tree';
import { keepClickedNodeSelected } from './listTreeSelection';

type ListTreeProps = Omit<TreeProps<DataNode>, 'expandedKeys' | 'onExpand'>;

const getNodeChildren = (node: DataNode, childrenFieldName: string): DataNode[] =>
  ((node as unknown as Record<string, unknown>)[childrenFieldName] as DataNode[] | undefined) ?? [];

const getNodeKey = (node: DataNode, keyFieldName: string): React.Key =>
  (node as unknown as Record<string, React.Key>)[keyFieldName] as React.Key;

const collectExpandableKeys = (
  nodes: DataNode[],
  keyFieldName: string,
  childrenFieldName: string,
): React.Key[] =>
  nodes.flatMap((node) => {
    const children = getNodeChildren(node, childrenFieldName);
    return [
      ...(children.length ? [getNodeKey(node, keyFieldName)] : []),
      ...collectExpandableKeys(children, keyFieldName, childrenFieldName),
    ];
  });

/**
 * 列表页左树的统一交互：点击未展开节点的标题时展开，收起只能通过左侧箭头触发。
 * 同时在异步树数据首次到达后补应用 defaultExpandAll，避免空数据首渲染吞掉默认展开。
 */
const ListTree = ({
  treeData = [],
  defaultExpandAll = false,
  defaultExpandedKeys = [],
  defaultSelectedKeys = [],
  fieldNames,
  onSelect,
  selectedKeys: controlledSelectedKeys,
  ...props
}: ListTreeProps) => {
  const [expandedKeysOverride, setExpandedKeysOverride] = useState<React.Key[]>();
  const [uncontrolledSelectedKeys, setUncontrolledSelectedKeys] = useState(defaultSelectedKeys);
  const keyFieldName = fieldNames?.key ?? 'key';
  const childrenFieldName = fieldNames?.children ?? 'children';
  // 首次用户交互前持续从最新异步数据派生默认值；交互后保持用户明确选择的展开状态。
  const expandedKeys =
    expandedKeysOverride ??
    (defaultExpandAll
      ? collectExpandableKeys(treeData, keyFieldName, childrenFieldName)
      : defaultExpandedKeys);

  return (
    <Tree<DataNode>
      {...props}
      treeData={treeData}
      fieldNames={fieldNames}
      expandedKeys={expandedKeys}
      selectedKeys={controlledSelectedKeys ?? uncontrolledSelectedKeys}
      onExpand={(keys) => setExpandedKeysOverride(keys)}
      onSelect={(keys, info) => {
        const nodeKey = getNodeKey(info.node, keyFieldName);
        // 列表筛选树的节点选择代表当前查询范围，重复点击只切换展开状态，不能清空范围。
        const nextSelectedKeys = keepClickedNodeSelected(keys, nodeKey);
        if (controlledSelectedKeys === undefined) {
          setUncontrolledSelectedKeys(nextSelectedKeys);
        }
        if (
          getNodeChildren(info.node, childrenFieldName).length &&
          !expandedKeys.includes(nodeKey)
        ) {
          setExpandedKeysOverride([...expandedKeys, nodeKey]);
        }
        onSelect?.(nextSelectedKeys, info);
      }}
    />
  );
};

export default ListTree;
