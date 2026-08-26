export const monitorPercent = (value?: number) => Math.max(0, Math.min(100, (value ?? 0) * 100));
export const monitorRatio = (used: number, total: number) =>
  total > 0 ? monitorPercent(used / total) : 0;
export const monitorBytes = (value?: number) => {
  const safe = value ?? 0;
  if (safe <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const index = Math.min(Math.floor(Math.log(safe) / Math.log(1024)), 4);
  return `${(safe / 1024 ** index).toFixed(index ? 1 : 0)} ${units[index]}`;
};
