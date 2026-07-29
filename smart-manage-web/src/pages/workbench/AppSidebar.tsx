import { useState } from 'react';
import type { MenuProps } from 'antd';
import { Empty, Menu, Skeleton } from 'antd';
import { FolderOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';
import type { MenuVO } from '@/types/api';
import { resolveIcon } from '@/domain/common/page/iconResolver';
import './AppSidebar.css';

interface Props {
  menuTree: MenuVO | null;
  loading: boolean;
  onItemClick: (item: MenuVO) => void;
}

/** 在菜单树中递归查找 */
function findMenuItem(items: MenuVO[], key: string): MenuVO | null {
  for (const item of items) {
    if ((item.path || item.name) === key) return item;
    if (item.routes?.length) {
      const found = findMenuItem(item.routes, key);
      if (found) return found;
    }
  }
  return null;
}

/** 递归渲染菜单项 */
function renderMenuTree(items: MenuVO[]): Required<MenuProps>['items'] {
  return items.map((item) => {
    const key = item.path || item.name;
    if (item.routes?.length) {
      return {
        key,
        label: item.name,
        icon: resolveIcon(item.icon) ?? <FolderOutlined />,
        children: renderMenuTree(item.routes),
        popupClassName: 'sm-workspace-sidebar-popup',
      };
    }
    return {
      key,
      label: item.name,
      icon: resolveIcon(item.icon),
    };
  });
}

const AppSidebar = ({ menuTree, loading, onItemClick }: Props) => {
  const [collapsed, setCollapsed] = useState(false);

  const handleClick: MenuProps['onClick'] = (info) => {
    if (!menuTree?.routes) return;
    const item = findMenuItem(menuTree.routes, info.key);
    // 只有叶子节点（有 component 的）才触发页面打开，父节点仅展开/折叠
    if (item?.component) onItemClick(item);
  };

  return (
    <div className={`sm-workspace-sidebar ${collapsed ? 'sm-workspace-sidebar--collapsed' : ''}`}>
      <div className="sm-workspace-sidebar-content">
        <div className="sm-workspace-sidebar-menu">
          {loading ? (
            <Skeleton className="sm-sidebar-skeleton" active paragraph={{ rows: 6 }} />
          ) : !menuTree?.routes?.length ? (
            <Empty description="暂无菜单" />
          ) : (
            <Menu
              mode="vertical"
              onClick={handleClick}
              items={renderMenuTree(menuTree.routes)}
              theme="dark"
              triggerSubMenuAction="hover"
            />
          )}
        </div>
        <div className="sm-workspace-sidebar-switch">
          <button
            className="sm-workspace-sidebar-switch-btn"
            type="button"
            onClick={() => setCollapsed((current) => !current)}
          >
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AppSidebar;
