import { useMemo, useState } from 'react';
import type { ColumnsType } from 'antd/es/table/interface';
import { useUserStore } from '@/stores/user';
import {
  applyColumnSettings,
  createColumnSettingsStorageKey,
  createDefaultColumnSettings,
  mergeColumnSettings,
  readStoredColumnSettings,
  removeStoredColumnSettings,
  resolveColumnSettings,
  writeStoredColumnSettings,
} from './columnSettings';
import type { ColumnSetting } from './columnSettings';

export function useListColumnSettings<T>(columns: ColumnsType<T>, columnSettingsKey?: string) {
  const userId = useUserStore((state) => state.userInfo?.id);
  const [open, setOpen] = useState(false);
  const defaultSettings = useMemo(() => createDefaultColumnSettings(columns), [columns]);
  const storageKey =
    columnSettingsKey && userId
      ? createColumnSettingsStorageKey(userId, columnSettingsKey)
      : undefined;
  const storedSettings = useMemo(
    () =>
      mergeColumnSettings(
        defaultSettings,
        storageKey ? readStoredColumnSettings(storageKey) : undefined,
      ),
    [defaultSettings, storageKey],
  );
  const [override, setOverride] = useState<{
    storageKey?: string;
    settings: ColumnSetting[] | null;
  }>();
  const settings =
    override && override.storageKey === storageKey
      ? resolveColumnSettings(defaultSettings, undefined, override.settings)
      : storedSettings;
  const displayedColumns = useMemo(
    () => (columnSettingsKey ? applyColumnSettings(columns, settings) : columns),
    [columnSettingsKey, columns, settings],
  );

  const confirm = (nextSettings: ColumnSetting[]) => {
    const restoredToDefaults = JSON.stringify(nextSettings) === JSON.stringify(defaultSettings);
    // 恢复出厂设置代表不存在用户覆盖，后续代码默认值变化必须立即生效。
    setOverride({ storageKey, settings: restoredToDefaults ? null : nextSettings });
    if (storageKey) {
      if (restoredToDefaults) removeStoredColumnSettings(storageKey);
      else writeStoredColumnSettings(storageKey, nextSettings);
    }
    setOpen(false);
  };

  return {
    displayedColumns,
    enabled: Boolean(columnSettingsKey),
    open,
    setOpen,
    settings,
    defaultSettings,
    confirm,
  };
}
