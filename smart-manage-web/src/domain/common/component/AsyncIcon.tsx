import { useEffect, useState } from 'react';
import type { ComponentType } from 'react';

interface AsyncIconProps {
  name: string;
  loadIcons: () => Promise<Record<string, ComponentType>>;
}

/** 按需补载不在常用白名单内的图标，避免全量图标进入主包。 */
function AsyncIcon({ name, loadIcons }: AsyncIconProps) {
  const [IconComponent, setIconComponent] = useState<ComponentType>();

  useEffect(() => {
    let active = true;
    void loadIcons()
      .then((icons) => {
        const loadedIcon = icons[name];
        if (active && loadedIcon) setIconComponent(() => loadedIcon);
      })
      .catch(() => {
        // 图标分包加载失败时保留空图标，避免影响菜单和页面主体渲染。
      });
    return () => {
      active = false;
    };
  }, [loadIcons, name]);

  return IconComponent ? <IconComponent /> : null;
}

export default AsyncIcon;
