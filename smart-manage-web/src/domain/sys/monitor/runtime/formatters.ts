export const monitorPercent = (value?: number | null) =>
  value == null ? null : Math.max(0, Math.min(100, value * 100));
export const monitorRatio = (used?: number | null, total?: number | null) =>
  used == null || total == null || total <= 0 ? null : monitorPercent(used / total);
export const monitorBytes = (value?: number | null) => {
  if (value == null) return '-';
  const safe = value;
  if (safe <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const index = Math.min(Math.floor(Math.log(safe) / Math.log(1024)), 4);
  return `${(safe / 1024 ** index).toFixed(index ? 1 : 0)} ${units[index]}`;
};

export const monitorHistoryValues = <T extends object>(
  points: T[],
  key: keyof T,
  multiplier?: number,
) =>
  points.map((item) => {
    const value = item[key];
    if (value == null || typeof value !== 'number') return null;
    return multiplier ? value * multiplier : value;
  });

export const monitorHealthPresentation = (status?: string | null) => {
  if (status === 'UP') return { color: 'success', text: 'UP（健康）' } as const;
  if (status === 'DOWN') return { color: 'error', text: 'DOWN（异常）' } as const;
  return { color: 'warning', text: 'UNKNOWN（状态暂不可用）' } as const;
};
