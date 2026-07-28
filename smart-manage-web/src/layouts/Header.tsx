import { useState } from 'react';
import { App, Avatar, Button, Dropdown, Popover, Tooltip } from 'antd';
import type { MenuProps } from 'antd';
import { UserOutlined, LogoutOutlined, SkinOutlined } from '@ant-design/icons';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useWorkbenchStore } from '@/stores/workbench';
import { useUserStore } from '@/stores/user';
import { openApp, closeAppAndRemove } from '@/services/navigationService';
import { logoutCurrentUser, updateCurrentUserTheme } from '@/api/user';
import { normalizeThemeColor, THEME_COLOR_OPTIONS } from '@/styles/themePalette';
import './Header.css';

const Header = () => {
  const { message, modal } = App.useApp();
  const [themeOpen, setThemeOpen] = useState(false);
  const [themeSaving, setThemeSaving] = useState(false);
  const tabs = useHeaderTabsStore((s) => s.tabs);
  const activeKey = useHeaderTabsStore((s) => s.activeKey);
  const userInfo = useUserStore((s) => s.userInfo);
  const clearUser = useUserStore((s) => s.clearUser);
  const setThemeColor = useUserStore((s) => s.setThemeColor);

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
      modal.confirm({
        title: '有未保存的数据',
        content: '部分页面存在未保存的修改，退出登录将丢失这些数据。确定退出吗？',
        okText: '确定退出',
        cancelText: '取消',
        okButtonProps: { danger: true },
        onOk: performLogout,
      });
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
      <div className="sm-header-logo">Smart Manage</div>

      {/* Header Tabs */}
      <nav className="sm-header-tabs" role="tablist" aria-label="应用切换">
        {tabs.map((tab) => (
          <div
            key={tab.key}
            role="tab"
            tabIndex={0}
            aria-selected={activeKey === tab.key}
            className={`sm-header-tab ${activeKey === tab.key ? 'sm-header-tab--active' : ''}`}
            onClick={() => handleTabClick(tab.key)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                handleTabClick(tab.key);
              }
            }}
          >
            <span>{tab.label}</span>
            {tab.closable && (
              <div className="sm-header-tab-operate">
                <button
                  className="sm-header-tab-operate-close"
                  onClick={(event) => handleRemove(event, tab.key)}
                  aria-label={`关闭 ${tab.label}`}
                >
                  ✕
                </button>
              </div>
            )}
          </div>
        ))}
      </nav>

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
          <Avatar size={32} className="sm-header-avatar" icon={<UserOutlined />}>
            {userInfo?.nickname?.[0]}
          </Avatar>
        </Dropdown>
      </div>
    </header>
  );
};

export default Header;
