/** 官方属性面板传数组，其他回显入口也可能传 @@ 分隔的权限串；统一为服务端有界查询协议。 */
export function feedbackPayload(parameters) {
  const storageIds = parameters?.storageIds;
  const rules = Array.isArray(storageIds) ? storageIds :
    typeof storageIds === 'string' ? storageIds.split('@@').filter(Boolean) : [];
  if (rules.some(rule => typeof rule !== 'string')) throw new Error('审批人标识格式无效');
  return { storageIds: rules.join(',') };
}

function coordinatePair(value) {
  if (typeof value !== 'string') return null;
  const parts = value.split(',');
  if (parts.length !== 2 || parts.some(part => !part.trim())) return null;
  const numbers = parts.map(Number);
  return numbers.every(Number.isFinite) ? numbers : null;
}

/**
 * 官方经典画布将缺少位置的 text 对象渲染成 NaN；在公开数据边界补齐文字锚点。
 * 只处理展示副本，不改写已发布定义或历史快照；仿钉钉模式沿用官方自动布局。
 */
export function designerDefinition(definition) {
  const result = structuredClone(definition);
  if (result.modelValue !== 'CLASSICS') return result;
  for (const node of result.nodeList ?? []) {
    const parts = typeof node.coordinate === 'string' ? node.coordinate.split('|') : [];
    const position = coordinatePair(parts[0]);
    if (!position) throw new Error(`节点「${node.nodeName ?? node.nodeCode}」坐标无效`);
    if (parts.length === 2 && coordinatePair(parts[1])) continue;
    const [nodeX, nodeY] = position;
    const textY = Number(node.nodeType) === 1 ? nodeY : nodeY + 40;
    node.coordinate = `${parts[0]}|${nodeX},${textY}`;
  }
  return result;
}
