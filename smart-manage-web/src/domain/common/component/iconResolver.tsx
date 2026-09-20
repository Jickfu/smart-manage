import type { ReactNode } from 'react';
import DynamicIcon from './DynamicIcon';
import { isSelectableIconName } from './iconCatalog';

export { isSelectableIconName, selectableIconNames } from './iconCatalog';

/** 未知名称使用调用方默认图标；合法名称从已初始化的受控矢量数据同步渲染。 */
export function resolveIcon(name: string | undefined, fallback?: ReactNode): ReactNode | undefined {
  if (!name || !isSelectableIconName(name)) return fallback;
  return <DynamicIcon name={name} fallback={fallback} />;
}
