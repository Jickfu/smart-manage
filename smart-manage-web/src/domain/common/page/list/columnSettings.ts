import type { ColumnType, ColumnsType } from 'antd/es/table/interface';

export type ColumnSettingsAlign = 'default' | 'left' | 'center' | 'right';
export type ColumnSettingsFixed = 'none' | 'left' | 'right';
export type ColumnWidthMode = 'auto' | 'fixed';

export interface ColumnSetting {
  key: string;
  label: string;
  hidden: boolean;
  align: ColumnSettingsAlign;
  fixed: ColumnSettingsFixed;
  widthMode: ColumnWidthMode;
  width?: number;
}

interface StoredColumnSettings {
  version: 1;
  columns: ColumnSetting[];
}

export const MIN_COLUMN_WIDTH = 40;
export const MAX_COLUMN_WIDTH = 600;

const getColumnLabel = (title: unknown, key: string) =>
  typeof title === 'string' || typeof title === 'number' ? String(title) : key;

export const getColumnSettingKey = <T>(column: ColumnType<T>): string | undefined => {
  if (typeof column.key === 'string' && column.key) return column.key;
  if (typeof column.dataIndex === 'string' && column.dataIndex) return column.dataIndex;
  return undefined;
};

export const createDefaultColumnSettings = <T>(columns: ColumnsType<T>): ColumnSetting[] =>
  columns.flatMap((column) => {
    if ('children' in column) return [];
    const key = getColumnSettingKey(column);
    if (!key) return [];
    const numericWidth = typeof column.width === 'number' ? column.width : undefined;
    return [
      {
        key,
        label: getColumnLabel(column.title, key),
        hidden: Boolean(column.hidden),
        align:
          column.align === 'left' || column.align === 'center' || column.align === 'right'
            ? column.align
            : 'default',
        fixed: column.fixed === 'left' || column.fixed === 'right' ? column.fixed : 'none',
        widthMode: numericWidth === undefined ? 'auto' : 'fixed',
        width: numericWidth,
      },
    ];
  });

const sanitizeSetting = (setting: ColumnSetting, fallback: ColumnSetting): ColumnSetting => {
  const width =
    setting.widthMode === 'fixed' && typeof setting.width === 'number'
      ? Math.min(MAX_COLUMN_WIDTH, Math.max(MIN_COLUMN_WIDTH, Math.round(setting.width)))
      : fallback.width;
  return {
    ...fallback,
    hidden: Boolean(setting.hidden),
    align: ['default', 'left', 'center', 'right'].includes(setting.align)
      ? setting.align
      : fallback.align,
    fixed: ['none', 'left', 'right'].includes(setting.fixed) ? setting.fixed : fallback.fixed,
    widthMode: setting.widthMode === 'auto' ? 'auto' : 'fixed',
    width,
  };
};

/** 按当前代码列定义合并本地配置：忽略已删除列，并把新增列按默认值补入。 */
export const mergeColumnSettings = (
  defaults: ColumnSetting[],
  stored: ColumnSetting[] | undefined,
): ColumnSetting[] => {
  if (!stored) return defaults;
  const defaultByKey = new Map(defaults.map((setting) => [setting.key, setting]));
  const merged = stored.flatMap((setting) => {
    const fallback = defaultByKey.get(setting.key);
    if (!fallback) return [];
    defaultByKey.delete(setting.key);
    return [sanitizeSetting(setting, fallback)];
  });
  return [...merged, ...defaultByKey.values()];
};

/** `null` 表示显式恢复出厂设置，此时完全跟随当前代码默认值。 */
export const resolveColumnSettings = (
  defaults: ColumnSetting[],
  stored: ColumnSetting[] | undefined,
  override: ColumnSetting[] | null | undefined,
): ColumnSetting[] =>
  override === null ? defaults : mergeColumnSettings(defaults, override ?? stored);

export const normalizeFixedColumnOrder = (settings: ColumnSetting[]): ColumnSetting[] => [
  ...settings.filter((setting) => setting.fixed === 'left'),
  ...settings.filter((setting) => setting.fixed === 'none'),
  ...settings.filter((setting) => setting.fixed === 'right'),
];

export const validateColumnSettings = (settings: ColumnSetting[]): string | undefined => {
  const visible = settings.filter((setting) => !setting.hidden);
  if (visible.length === 0) return '至少需要显示一个业务列';
  if (
    settings.some(
      (setting) =>
        setting.widthMode === 'fixed' &&
        (typeof setting.width !== 'number' ||
          setting.width < MIN_COLUMN_WIDTH ||
          setting.width > MAX_COLUMN_WIDTH),
    )
  ) {
    return `固定列宽必须设置为 ${MIN_COLUMN_WIDTH}–${MAX_COLUMN_WIDTH}px`;
  }
  if (!visible.some((setting) => setting.fixed === 'none' && setting.widthMode === 'auto')) {
    return '至少保留一个未冻结的自动宽度列，用于吸收表格剩余空间';
  }
  return undefined;
};

export const applyColumnSettings = <T>(
  columns: ColumnsType<T>,
  settings: ColumnSetting[],
): ColumnsType<T> => {
  const columnByKey = new Map(
    columns.flatMap((column) => {
      if ('children' in column) return [];
      const key = getColumnSettingKey(column);
      return key ? [[key, column] as const] : [];
    }),
  );
  return normalizeFixedColumnOrder(settings).flatMap((setting) => {
    const column = columnByKey.get(setting.key);
    if (!column) return [];
    const columnWithoutWidth = { ...column };
    delete columnWithoutWidth.width;
    return [
      {
        ...columnWithoutWidth,
        key: setting.key,
        hidden: setting.hidden,
        align: setting.align === 'default' ? undefined : setting.align,
        fixed: setting.fixed === 'none' ? undefined : setting.fixed,
        ...(setting.widthMode === 'fixed' ? { width: setting.width ?? MIN_COLUMN_WIDTH } : {}),
      },
    ];
  });
};

export const moveColumnSetting = (
  settings: ColumnSetting[],
  key: string,
  direction: 'up' | 'down' | 'top' | 'bottom',
): ColumnSetting[] => {
  const currentIndex = settings.findIndex((setting) => setting.key === key);
  if (currentIndex < 0) return settings;
  const current = settings[currentIndex]!;
  const groupIndexes = settings.flatMap((setting, index) =>
    setting.fixed === current.fixed ? [index] : [],
  );
  const groupPosition = groupIndexes.indexOf(currentIndex);
  const targetGroupPosition =
    direction === 'top'
      ? 0
      : direction === 'bottom'
        ? groupIndexes.length - 1
        : direction === 'up'
          ? Math.max(0, groupPosition - 1)
          : Math.min(groupIndexes.length - 1, groupPosition + 1);
  const targetIndex = groupIndexes[targetGroupPosition]!;
  if (targetIndex === currentIndex) return settings;
  const next = [...settings];
  next.splice(currentIndex, 1);
  next.splice(targetIndex, 0, current);
  return next;
};

export const readStoredColumnSettings = (storageKey: string): ColumnSetting[] | undefined => {
  try {
    const raw = localStorage.getItem(storageKey);
    if (!raw) return undefined;
    const parsed = JSON.parse(raw) as Partial<StoredColumnSettings>;
    return parsed.version === 1 && Array.isArray(parsed.columns) ? parsed.columns : undefined;
  } catch {
    return undefined;
  }
};

export const writeStoredColumnSettings = (storageKey: string, columns: ColumnSetting[]) => {
  const payload: StoredColumnSettings = { version: 1, columns };
  localStorage.setItem(storageKey, JSON.stringify(payload));
};

export const removeStoredColumnSettings = (storageKey: string) =>
  localStorage.removeItem(storageKey);

export const createColumnSettingsStorageKey = (userId: string, pageKey: string) =>
  `smart-manage:list-columns:${userId}:${pageKey}`;
