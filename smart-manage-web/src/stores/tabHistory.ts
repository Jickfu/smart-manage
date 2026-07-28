/**
 * 将页签追加到激活历史末尾，并确保同一页签只保留最近一次记录。
 */
export function pushTabHistory(history: string[], activeKey: string): string[] {
  return [...history.filter((historyKey) => historyKey !== activeKey), activeKey];
}

/**
 * 从激活历史中反向寻找最近仍可用的页签。
 */
export function resolveNextActiveTabKey(
  history: string[],
  availableKeys: ReadonlySet<string>,
  fallbackKey: string,
  excludedKeys: ReadonlySet<string> = new Set(),
): string {
  return (
    [...history]
      .reverse()
      .find((historyKey) => !excludedKeys.has(historyKey) && availableKeys.has(historyKey)) ??
    fallbackKey
  );
}
