/** 将受控引用值转换为稳定的单行显示文本。 */
export function formatRefSelectorDisplayText<T>(
  value: T | T[] | null | undefined,
  displayRender: (record: T) => string,
): string {
  if (value == null) return '';
  return (Array.isArray(value) ? value : [value]).map(displayRender).join('，');
}

/** 给溢出判断保留 2px 安全距离，避免亚像素取整导致计数闪烁。 */
export function isRefSelectorTextOverflowing(
  scrollWidth: number,
  inputWidth: number,
  selectionTotalWidth = 0,
): boolean {
  return scrollWidth > inputWidth + selectionTotalWidth + 2;
}
