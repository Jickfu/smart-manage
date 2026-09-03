export const keepClickedNodeSelected = (
  selectedKeys: React.Key[],
  clickedNodeKey: React.Key,
): React.Key[] => (selectedKeys.length ? selectedKeys : [clickedNodeKey]);
