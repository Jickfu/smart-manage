import { useEffect, useState } from 'react';
import { App, Button, Dropdown, Popover, Tooltip } from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { MenuProps } from 'antd';
import { LogoutOutlined, SkinOutlined } from '@ant-design/icons';
import { UserAvatar } from '@/domain/sys/base/user/UserAvatar';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useWorkbenchStore } from '@/stores/workbench';
import { useUserStore } from '@/stores/user';
import { openApp, closeAppAndRemove } from '@/services/navigationService';
import { logoutCurrentUser, updateCurrentUserTheme } from '@/api/user';
import { normalizeThemeColor, THEME_COLOR_OPTIONS } from '@/styles/themePalette';
import { activeUiConfigQueryKey, getActiveUiConfig } from '@/api/uiConfig';
import { resolveAssetUrl } from '@/utils/assetUrl';
import HeaderTabs from './HeaderTabs';
import './Header.css';

const Header = () => {
  const { message } = App.useApp();
  const [themeOpen, setThemeOpen] = useState(false);
  const [themeSaving, setThemeSaving] = useState(false);
  const tabs = useHeaderTabsStore((s) => s.tabs);
  const activeKey = useHeaderTabsStore((s) => s.activeKey);
  const userInfo = useUserStore((s) => s.userInfo);
  const clearUser = useUserStore((s) => s.clearUser);
  const setThemeColor = useUserStore((s) => s.setThemeColor);
  const uiConfigQuery = useQuery({
    queryKey: activeUiConfigQueryKey,
    queryFn: getActiveUiConfig,
  });
  const pageTitle = uiConfigQuery.data?.pageTitle?.trim() || 'Smart Manage';
  const systemName = uiConfigQuery.data?.systemName?.trim() || 'Smart Manage';
  const headerLogo = resolveAssetUrl(uiConfigQuery.data?.headerLogo, '/logo.svg');

  useEffect(() => {
    document.title = pageTitle;
  }, [pageTitle]);

  const handleTabClick = (key: string) => {
    openApp(key);
  };

  const handleRemove = async (event: React.MouseEvent, key: string) => {
    event.stopPropagation();
    await closeAppAndRemove(key);
  };

  const handleLogout = async () => {
    const allowed = await useWorkbenchStore.getState().checkAllDirty();
    if (!allowed) {
      // 页面关闭守卫已经完成用户确认；拒绝关闭时必须尊重该决定，不能再次弹窗绕过。
      return;
    }
    await performLogout();
  };

  /** 先使服务端 token 失效，成功后再清理浏览器认证状态。 */
  const performLogout = async () => {
    try {
      await logoutCurrentUser();
      clearUser();
      window.location.href = '/login.html';
    } catch (error) {
      message.error(error instanceof Error ? error.message : '退出登录失败');
    }
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  const handleThemeChange = async (themeColor: string) => {
    if (
      themeSaving ||
      (userInfo?.themeColor && normalizeThemeColor(userInfo.themeColor) === themeColor)
    ) {
      setThemeOpen(false);
      return;
    }
    setThemeSaving(true);
    try {
      await updateCurrentUserTheme(themeColor);
      setThemeColor(themeColor);
      setThemeOpen(false);
      message.success('个人主题已更新');
    } finally {
      setThemeSaving(false);
    }
  };

  const themePicker = (
    <div className="sm-theme-picker">
      <div className="sm-theme-picker-title">选择主题色</div>
      <div className="sm-theme-picker-grid">
        {THEME_COLOR_OPTIONS.map((option) => {
          const selected = normalizeThemeColor(userInfo?.themeColor) === option.value;
          return (
            <Tooltip key={option.key} title={option.label}>
              <button
                type="button"
                className={`sm-theme-color ${selected ? 'sm-theme-color--selected' : ''}`}
                aria-label={option.label}
                aria-pressed={selected}
                disabled={themeSaving}
                onClick={() => void handleThemeChange(option.value)}
              >
                <span className={`sm-theme-color-swatch sm-theme-color-swatch--${option.key}`} />
              </button>
            </Tooltip>
          );
        })}
      </div>
    </div>
  );

  return (
    <header className="sm-header">
      <img className="sm-header-logo" src={headerLogo} alt={systemName} />

      {/* Header Tabs */}
      <HeaderTabs
        tabs={tabs}
        activeKey={activeKey}
        onActivate={handleTabClick}
        onRemove={(event, key) => void handleRemove(event, key)}
      />
      {/* 右侧操作区 */}
      <div className="sm-header-actions">
        <Popover
          content={themePicker}
          trigger="click"
          placement="bottomRight"
          open={themeOpen}
          onOpenChange={setThemeOpen}
        >
          <Button
            className="sm-header-action-button"
            type="text"
            icon={<SkinOutlined />}
            aria-label="切换个人主题色"
          />
        </Popover>
        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
          <UserAvatar
            size={32}
            className="sm-header-avatar"
            src={userInfo?.avatar}
            name={userInfo?.name}
            username={userInfo?.username}
          />
        </Dropdown>
      </div>
    </header>
  );
};

export default Header;
