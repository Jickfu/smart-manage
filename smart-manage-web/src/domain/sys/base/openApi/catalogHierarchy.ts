import type { OpenApiCatalogHierarchyNode, OpenApiListForm } from './types';

export function buildCatalogTree(nodes: OpenApiCatalogHierarchyNode[]) {
  const nodeByKey = new Map(
    nodes.map((node) => [node.key, { ...node, children: [] as OpenApiCatalogHierarchyNode[] }]),
  );
  const roots: OpenApiCatalogHierarchyNode[] = [];
  for (const node of nodeByKey.values()) {
    const parent = node.parentKey ? nodeByKey.get(node.parentKey) : undefined;
    if (parent) parent.children?.push(node);
    else roots.push(node);
  }
  return roots;
}

export function catalogFilterFromTreeKey(parentId?: string): Partial<OpenApiListForm> {
  if (!parentId) return {};
  const separatorIndex = parentId.indexOf(':');
  const type = parentId.slice(0, separatorIndex);
  const parts = parentId.slice(separatorIndex + 1).split('/');
  if (type === 'domain') return { domainKey: parts[0] };
  if (type === 'application') return { domainKey: parts[0], applicationKey: parts[1] };
  if (type === 'feature') {
    return { domainKey: parts[0], applicationKey: parts[1], featureKey: parts[2] };
  }
  return {};
}
