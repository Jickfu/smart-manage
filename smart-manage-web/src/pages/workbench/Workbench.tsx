import { memo, useCallback } from 'react';
import { App, Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { menuQueryKeys } from '@/domain/sys/base/menu/queryKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { getUserMenusByAppNumber } from '@/domain/sys/base/menu/api';
import AppSidebar from './AppSidebar';
import ContentTabsBar from './ContentTabsBar';
import PageRenderer from './PageRenderer';
import ApplicationHome from './ApplicationHome';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { MenuVO } from '@/types/api';
import ExternalLinkFrame from './ExternalLinkFrame';
import { resolveMenuAction } from './menuNavigation';
import './Workbench.css';

interface Props {
  appNumber: string;
}

const Workbench = ({ appNumber }: Props) => {
  const { message } = App.useApp();
  const ws = useWorkbenchStore((s) => s.workspaces[appNumber]);
  const openListTab = useWorkbenchStore((s) => s.openListTab);
  const openCustomTab = useWorkbenchStore((s) => s.openCustomTab);
  const openExternalLinkTab = useWorkbenchStore((s) => s.openExternalLinkTab);

  const menuQuery = useQuery({
    queryKey: menuQueryKeys.userByApp(appNumber),
    queryFn: () => getUserMenusByAppNumber(appNumber),
    staleTime: 5 * 60 * 1000,
  });

  const handleMenuItemClick = useCallback(
    (item: MenuVO) => {
      try {
        const action = resolveMenuAction(item);
        if (action.type === 'EXTERNAL_NEW_TAB') {
          window.open(action.externalUrl, '_blank', 'noopener,noreferrer');
          return;
        }
        if (action.type === 'EXTERNAL_IFRAME') {
          openExternalLinkTab(appNumber, action.menuId, action.title, action.externalUrl);
          return;
        }
        if (componentRegistry[action.componentKey]?.pageType === 'CUSTOM') {
          openCustomTab(appNumber, action.componentKey);
        } else {
          openListTab(appNumber, action.componentKey);
        }
      } catch (error) {
        message.error(error instanceof Error ? error.message : '菜单配置无效');
      }
    },
    [appNumber, message, openCustomTab, openExternalLinkTab, openListTab],
  );

  if (!ws) return null;

  return (
    <div className="sm-workspace">
      <AppSidebar
        menuTree={menuQuery.data ?? null}
        loading={menuQuery.isLoading}
        onItemClick={handleMenuItemClick}
      />
      <div className="sm-workspace-body">
        <ContentTabsBar appNumber={appNumber} />
        <Spin spinning={menuQuery.isLoading}>
          <ol className="sm-workspace-content">
            {ws.contentTabs.map((tab) => {
              const isActive = ws.activeContentTabKey === tab.key;
              return (
                <li
                  key={tab.key}
                  className={`sm-content-pane ${isActive ? 'sm-content-pane--active' : ''}`}
                >
                  {tab.key === '__home__' ? (
                    <ApplicationHome appNumber={appNumber} appName={ws.appInfo.name} />
                  ) : tab.externalUrl ? (
                    <ExternalLinkFrame title={tab.label} externalUrl={tab.externalUrl} />
                  ) : (
                    <PageRenderer
                      appNumber={appNumber}
                      tabKey={tab.key}
                      title={tab.label}
                      componentKey={tab.componentKey}
                      pageType={tab.pageType}
                      operationType={tab.operationType}
                      billId={tab.billId}
                      context={tab.context}
                      temporary={tab.temporary}
                      active={isActive}
                    />
                  )}
                </li>
              );
            })}
          </ol>
        </Spin>
      </div>
    </div>
  );
};

export default memo(Workbench);
