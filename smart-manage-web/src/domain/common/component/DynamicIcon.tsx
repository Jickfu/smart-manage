import { useCallback, useEffect, useSyncExternalStore } from 'react';
import type { ReactNode } from 'react';
import { QuestionCircleOutlined } from '@ant-design/icons';
import { Spin } from 'antd';
import { getIconSnapshot, loadIcon, subscribeIcon } from './iconCatalog';

/** 已缓存图标首帧直接显示；冷加载只更新自身，不让外层页面进入 Suspense。 */
export default function DynamicIcon({ name, fallback }: { name: string; fallback?: ReactNode }) {
  const subscribe = useCallback((listener: () => void) => subscribeIcon(name, listener), [name]);
  const getSnapshot = useCallback(() => getIconSnapshot(name), [name]);
  const icon = useSyncExternalStore(subscribe, getSnapshot);
  useEffect(() => {
    void loadIcon(name);
  }, [name]);
  if (icon) return icon;
  if (icon === null) {
    return fallback ?? <QuestionCircleOutlined title="图标加载失败，请刷新页面" />;
  }
  return <Spin size="small" aria-label="图标加载中" />;
}
