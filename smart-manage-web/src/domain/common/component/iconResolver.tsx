import type { ReactNode } from 'react';
import DynamicIcon from './DynamicIcon';
import { isSelectableIconName } from './iconCatalog';

export { isSelectableIconName, selectableIconNames, preloadIcons } from './iconCatalog';

/** 未知名称使用调用方默认图标；合法名称复用按需加载缓存。 */
export function resolveIcon(name: string | undefined, fallback?: ReactNode): ReactNode | undefined {
  if (!name || !isSelectableIconName(name)) return fallback;
  return <DynamicIcon name={name} fallback={fallback} />;
}
