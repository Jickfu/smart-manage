/** 仅格式化调用方已获授权的安全对象标识；不读取记录或推断业务字段。 */
export function formatOperationTargets(labels: readonly string[]): string {
  const preview = labels
    .slice(0, 5)
    .map((label) => JSON.stringify(label))
    .join('、');
  // 大批量操作明确总范围与摘要数量，避免把全部记录撑入确认弹框。
  const remaining = labels.length > 5 ? `；另有 ${labels.length - 5} 项未展开` : '';
  return `共 ${labels.length} 项：${preview}${remaining}。`;
}
