import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useEffect, useMemo, useState } from 'react';
import { Button, Input, Popover, Table, Tooltip, Tree } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { UserAvatar } from '@/domain/sys/base/user/UserAvatar';
import AppModal from '@/domain/common/component/AppModal';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useWorkbenchStore } from '@/stores/workbench';
import { useUserStore } from '@/stores/user';
import { openApp, closeAppAndRemove } from '@/services/navigationService';
import { pinApp, unpinApp } from '@/domain/sys/base/user/appPinApi';
import type { HeaderTabItem } from '@/stores/headerTabs';
import {
  logoutCurrentUser,
  switchCurrentUserOrganization,
  updateCurrentUserTheme,
} from '@/api/user';
import { normalizeThemeColor, THEME_COLOR_OPTIONS } from '@/styles/themePalette';
import { activeUiConfigQueryKey, getActiveUiConfig } from '@/api/uiConfig';
import { resolveAssetUrl } from '@/utils/assetUrl';
import HeaderTabs from './HeaderTabs';
import HeaderUserPanel from './HeaderUserPanel';
import PersonalSettingsModal from './PersonalSettingsModal';
import InboxHeaderButton from '@/domain/sys/message/inbox/InboxHeaderButton';
import ListTableShell from '@/domain/common/page/list/ListTableShell';
import './Header.css';

interface OrganizationTreeNode {
  key: string;
  title: string;
  children: OrganizationTreeNode[];
}

const Header = () => {
  const feedback = useOperationFeedback();
  const [themeOpen, setThemeOpen] = useState(false);
  const [themeSaving, setThemeSaving] = useState(false);
  const [userPanelOpen, setUserPanelOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [organizationOpen, setOrganizationOpen] = useState(false);
  const [aboutOpen, setAboutOpen] = useState(false);
  const [selectedOrgId, setSelectedOrgId] = useState<string>();
  const [selectedOrgPath, setSelectedOrgPath] = useState<string>();
  const [organizationKeyword, setOrganizationKeyword] = useState('');
  const [organizationSaving, setOrganizationSaving] = useState(false);
  const [pinSavingKeys, setPinSavingKeys] = useState<ReadonlySet<string>>(new Set());
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

  const handlePinToggle = async (event: React.MouseEvent, tab: HeaderTabItem) => {
    event.stopPropagation();
    if (pinSavingKeys.has(tab.key)) return;
    setPinSavingKeys((keys) => new Set(keys).add(tab.key));
    try {
      if (tab.pinned) {
        await unpinApp(tab.key);
      } else {
        await pinApp(tab.key);
      }
      useHeaderTabsStore.getState().setAppPinned(tab.key, !tab.pinned);
    } catch (error) {
      feedback.fromError(error, '应用固定状态更新失败');
    } finally {
      setPinSavingKeys((keys) => {
        const nextKeys = new Set(keys);
        nextKeys.delete(tab.key);
        return nextKeys;
      });
    }
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
      feedback.fromError(error, '退出登录失败');
    }
  };

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
      feedback.success('个人主题已更新');
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

  const openPanelModal = (openModal: () => void) => {
    setUserPanelOpen(false);
    setThemeOpen(false);
    openModal();
  };

  const openOrganizationModal = () => {
    setSelectedOrgId(userInfo?.currentOrgId);
    setSelectedOrgPath(undefined);
    setOrganizationKeyword('');
    openPanelModal(() => setOrganizationOpen(true));
  };

  const organizationTree = useMemo(() => {
    const roots: OrganizationTreeNode[] = [];
    for (const assignment of userInfo?.assignments ?? []) {
      const segments = assignment.orgNamePath.split('/').filter(Boolean);
      let siblings = roots;
      let path = '';
      for (const segment of segments) {
        path = path ? `${path}/${segment}` : segment;
        let node = siblings.find((item) => item.key === path);
        if (!node) {
          node = { key: path, title: segment, children: [] };
          siblings.push(node);
        }
        siblings = node.children;
      }
    }
    return roots;
  }, [userInfo?.assignments]);

  const visibleOrganizations = useMemo(() => {
    const keyword = organizationKeyword.trim().toLowerCase();
    return (userInfo?.assignments ?? []).filter(
      (assignment) =>
        (!selectedOrgPath || assignment.orgNamePath.startsWith(selectedOrgPath)) &&
        (!keyword ||
          assignment.org.name.toLowerCase().includes(keyword) ||
          assignment.orgNamePath.toLowerCase().includes(keyword)),
    );
  }, [organizationKeyword, selectedOrgPath, userInfo?.assignments]);

  const handleOrganizationSwitch = async (targetOrgId = selectedOrgId) => {
    if (!targetOrgId || targetOrgId === userInfo?.currentOrgId) {
      setOrganizationOpen(false);
      return;
    }
    const allowed = await useWorkbenchStore.getState().checkAllDirty();
    if (!allowed) return;
    setOrganizationSaving(true);
    try {
      await switchCurrentUserOrganization(targetOrgId);
      // 组织上下文影响应用、菜单和权限，刷新可确保所有服务端状态按新组织重新加载。
      window.location.reload();
    } catch (error) {
      feedback.fromError(error, '切换组织失败');
      setOrganizationSaving(false);
    }
  };

  return (
    <header className="sm-header">
      <img className="sm-header-logo" src={headerLogo} alt={systemName} />

      {/* Header Tabs */}
      <HeaderTabs
        tabs={tabs}
        activeKey={activeKey}
        onActivate={handleTabClick}
        onRemove={(event, key) => void handleRemove(event, key)}
        onPinToggle={(event, tab) => void handlePinToggle(event, tab)}
        pinSavingKeys={pinSavingKeys}
      />
      {/* 右侧操作区 */}
      <div className="sm-header-actions">
        <InboxHeaderButton />
        <Popover
          content={
            <HeaderUserPanel
              userInfo={userInfo}
              themePicker={themePicker}
              themeOpen={themeOpen}
              onThemeOpenChange={setThemeOpen}
              onOpenProfile={() => openPanelModal(() => setProfileOpen(true))}
              onOpenOrganization={openOrganizationModal}
              onOpenAbout={() => openPanelModal(() => setAboutOpen(true))}
              onLogout={() => {
                setUserPanelOpen(false);
                void handleLogout();
              }}
            />
          }
          trigger="click"
          placement="bottomRight"
          align={{ offset: [12, -4] }}
          arrow={false}
          rootClassName="sm-user-panel-popover"
          open={userPanelOpen}
          onOpenChange={setUserPanelOpen}
        >
          <button type="button" className="sm-header-avatar-button" aria-label="打开用户菜单">
            <UserAvatar
              size={32}
              className="sm-header-avatar"
              src={userInfo?.avatar}
              name={userInfo?.name}
              username={userInfo?.username}
            />
          </button>
        </Popover>
      </div>

      <PersonalSettingsModal
        open={profileOpen}
        userInfo={userInfo}
        onClose={() => setProfileOpen(false)}
        onProfileSaved={(profile) => {
          if (userInfo) {
            useUserStore.getState().setUserInfo({
              ...userInfo,
              name: profile.name,
              avatar: profile.avatar,
              avatarAttachmentId: profile.avatarAttachmentId
                ? String(profile.avatarAttachmentId)
                : undefined,
              phone: profile.phone,
              email: profile.email,
              gender: profile.gender,
              birthday: profile.birthday,
            });
          }
        }}
        onPasswordChanged={() => {
          clearUser();
          window.location.href = '/login.html';
        }}
      />

      <AppModal
        title="切换组织"
        open={organizationOpen}
        className="sm-organization-modal"
        bodyMode="fixed"
        width={900}
        headerExtra={
          <Input.Search
            variant="underlined"
            className="sm-organization-search"
            placeholder="搜索组织"
            value={organizationKeyword}
            onChange={(event) => setOrganizationKeyword(event.target.value)}
          />
        }
        closeDisabled={organizationSaving}
        onCancel={() => setOrganizationOpen(false)}
        footer={
          <>
            <Button disabled={organizationSaving} onClick={() => setOrganizationOpen(false)}>
              取消
            </Button>
            <Button
              type="primary"
              loading={organizationSaving}
              disabled={!selectedOrgId}
              onClick={() => void handleOrganizationSwitch()}
            >
              确定
            </Button>
          </>
        }
      >
        <ListTableShell
          total={visibleOrganizations.length}
          selectedCount={
            selectedOrgId &&
            visibleOrganizations.some((organization) => organization.org.id === selectedOrgId)
              ? 1
              : 0
          }
          showPagination={false}
          treePanel={
            <div className="sm-organization-tree">
              <Tree
                treeData={organizationTree}
                defaultExpandAll
                blockNode
                selectedKeys={selectedOrgPath ? [selectedOrgPath] : []}
                onSelect={(keys) => setSelectedOrgPath(keys[0] ? String(keys[0]) : undefined)}
              />
            </div>
          }
          table={
            <Table
              className="sm-list-table sm-organization-table"
              size="small"
              rowKey={(record) => record.org.id}
              pagination={false}
              dataSource={visibleOrganizations}
              rowSelection={{
                type: 'radio',
                selectedRowKeys: selectedOrgId ? [selectedOrgId] : [],
                onChange: (keys) => setSelectedOrgId(keys[0] ? String(keys[0]) : undefined),
              }}
              columns={[
                { title: '组织名称', render: (_, record) => record.org.name, width: 180 },
                { title: '组织长名称', dataIndex: 'orgNamePath' },
                {
                  title: '岗位',
                  dataIndex: 'position',
                  width: 120,
                  render: (value) => value || '-',
                },
              ]}
              onRow={(record) => ({
                onClick: () => setSelectedOrgId(record.org.id),
                onDoubleClick: () => {
                  setSelectedOrgId(record.org.id);
                  void handleOrganizationSwitch(record.org.id);
                },
              })}
              sticky
              tableLayout="fixed"
              scroll={{ x: 'max-content', y: 1 }}
            />
          }
        />
      </AppModal>

      <AppModal
        title="关于产品"
        open={aboutOpen}
        width={440}
        bodyMode="natural"
        onCancel={() => setAboutOpen(false)}
        footer={<Button onClick={() => setAboutOpen(false)}>关闭</Button>}
      >
        <div className="sm-about-product">
          <img src={headerLogo} alt={systemName} />
          <strong>{systemName}</strong>
          <span>模块化企业管理平台</span>
        </div>
      </AppModal>
    </header>
  );
};

export default Header;
