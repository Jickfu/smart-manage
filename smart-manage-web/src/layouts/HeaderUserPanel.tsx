import { Button, Popover, Tooltip } from 'antd';
import { LogoutOutlined, SettingOutlined, SwapOutlined } from '@ant-design/icons';
import type { UserInfoVO } from '@/types/api';
import type { ReactNode } from 'react';
import { UserAvatar } from '@/domain/sys/base/user/UserAvatar';

interface HeaderUserPanelProps {
  userInfo: UserInfoVO | null;
  themePicker: ReactNode;
  themeOpen: boolean;
  onThemeOpenChange: (open: boolean) => void;
  onOpenProfile: () => void;
  onOpenOrganization: () => void;
  onOpenAbout: () => void;
  onLogout: () => void;
}

/** 头像弹层只编排当前用户入口，具体命令与弹框状态由 Header 统一管理。 */
export default function HeaderUserPanel({
  userInfo,
  themePicker,
  themeOpen,
  onThemeOpenChange,
  onOpenProfile,
  onOpenOrganization,
  onOpenAbout,
  onLogout,
}: HeaderUserPanelProps) {
  return (
    <div className="sm-user-panel">
      <div className="sm-user-panel-hero">
        <div className="sm-user-panel-actions">
          <Tooltip title="个人设置">
            <Button
              type="text"
              shape="circle"
              icon={<SettingOutlined />}
              aria-label="打开个人设置"
              onClick={onOpenProfile}
            />
          </Tooltip>
          <Tooltip title="退出登录" placement="bottomRight">
            <Button
              type="text"
              shape="circle"
              icon={<LogoutOutlined />}
              aria-label="退出登录"
              onClick={onLogout}
            />
          </Tooltip>
        </div>
        <div className="sm-user-panel-identity">
          <UserAvatar
            size={44}
            src={userInfo?.avatar}
            name={userInfo?.name}
            username={userInfo?.username}
          />
          <div className="sm-user-panel-identity-text">
            <strong>{userInfo?.name || userInfo?.username || '当前用户'}</strong>
            <span>{userInfo?.companyName || 'Smart Manage'}</span>
          </div>
        </div>
        <div className="sm-user-panel-motto">心有所向，日复一日，必有精进。</div>
      </div>

      <div className="sm-user-panel-content">
        <div className="sm-user-panel-row">
          <span className="sm-user-panel-label">组织</span>
          <span className="sm-user-panel-value" title={userInfo?.currentOrgName}>
            {userInfo?.currentOrgName || '未设置'}
          </span>
          <Tooltip title="切换组织">
            <Button
              type="text"
              shape="circle"
              size="small"
              icon={<SwapOutlined />}
              aria-label="切换组织"
              onClick={onOpenOrganization}
            />
          </Tooltip>
        </div>
        <div className="sm-user-panel-row">
          <span className="sm-user-panel-label">主题</span>
          <span />
          <Popover
            content={themePicker}
            trigger="click"
            placement="leftTop"
            open={themeOpen}
            onOpenChange={onThemeOpenChange}
          >
            <Button
              className="sm-user-panel-theme-button"
              type="text"
              shape="circle"
              size="small"
              aria-label="切换个人主题色"
            >
              <span className="sm-user-panel-theme-swatch" />
            </Button>
          </Popover>
        </div>
      </div>

      <Button className="sm-user-panel-about" type="link" size="small" onClick={onOpenAbout}>
        关于产品
      </Button>
    </div>
  );
}
