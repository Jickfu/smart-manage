export function formatCpuUsage(value: number | null | undefined) {
  return value == null ? '-' : `${value.toFixed(2)}%`;
}
