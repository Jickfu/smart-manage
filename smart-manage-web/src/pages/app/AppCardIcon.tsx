import { useCallback } from 'react';
import type { RefCallback } from 'react';
import { AppstoreOutlined } from '@ant-design/icons';
import { resolveIcon } from '@/domain/common/component/iconResolver';

interface AppCardIconProps {
  icon: string | undefined;
  iconColor: string | undefined;
}

/** 应用图标颜色来自服务端配置，必须在运行时写入，不能固化为 CSS 枚举。 */
function AppCardIcon({ icon, iconColor }: AppCardIconProps) {
  const bindIconColor = useCallback<RefCallback<HTMLSpanElement>>(
    (element) => {
      if (!element) return;
      element.style.color = iconColor?.trim() || '';
    },
    [iconColor],
  );

  return (
    <span ref={bindIconColor} className="sm-app-card-icon" aria-hidden="true">
      {resolveIcon(icon?.trim(), <AppstoreOutlined />)}
    </span>
  );
}

export default AppCardIcon;
