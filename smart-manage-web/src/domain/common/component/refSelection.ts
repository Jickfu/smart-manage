export function createRefSelection<T extends Record<string, unknown>>(
  value: T | T[] | null | undefined,
  multiple: boolean,
  keyField: string,
): Map<string, T> {
  const selection = new Map<string, T>();
  if (value == null) return selection;
  if (multiple && Array.isArray(value)) {
    for (const record of value) selection.set(String(record[keyField]), record);
    return selection;
  }
  if (!multiple) {
    const record = value as T;
    selection.set(String(record[keyField]), record);
  }
  return selection;
}

export function mergeRefTableSelection<T extends Record<string, unknown>>(
  previous: Map<string, T>,
  previousKeys: React.Key[],
  nextKeys: React.Key[],
  currentPageRows: T[],
  keyField: string,
): Map<string, T> {
  const next = new Map(previous);
  for (const key of previousKeys) {
    if (!nextKeys.includes(key)) next.delete(String(key));
  }
  for (const key of nextKeys) {
    if (previousKeys.includes(key)) continue;
    const record = currentPageRows.find((item) => String(item[keyField]) === String(key));
    if (record) next.set(String(key), record);
  }
  return next;
}
