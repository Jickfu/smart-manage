import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { memo, useCallback, useEffect, useRef } from 'react';
import { Spin } from 'antd';
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
import { findMenuEntry, resolveMenuAction } from './menuNavigation';
import './Workbench.css';

interface Props {
  appNumber: string;
  initialEntryNumber?: string;
  onInitialEntryConsumed: () => void;
}

const Workbench = ({ appNumber, initialEntryNumber, onInitialEntryConsumed }: Props) => {
  const feedback = useOperationFeedback();
  const startupEntryConsumed = useRef(false);
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
    (item: MenuVO, startupNavigation = false) => {
      try {
        const action = resolveMenuAction(item);
        if (action.type === 'EXTERNAL_NEW_TAB') {
          if (startupNavigation) {
            feedback.info(`“${item.name}”是外部链接，请从左侧菜单点击打开`);
            return;
          }
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
        feedback.fromError(error, '菜单配置无效');
      }
    },
    [appNumber, feedback, openCustomTab, openExternalLinkTab, openListTab],
  );

  useEffect(() => {
    if (!initialEntryNumber || startupEntryConsumed.current) return;
    if (menuQuery.isError) {
      startupEntryConsumed.current = true;
      onInitialEntryConsumed();
      feedback.warning('入口菜单加载失败，已停留在应用首页');
      return;
    }
    if (!menuQuery.isSuccess) return;

    startupEntryConsumed.current = true;
    onInitialEntryConsumed();
    const entry = findMenuEntry(menuQuery.data.routes ?? [], initialEntryNumber);
    if (!entry) {
      feedback.warning('指定入口不存在或当前账号无权访问，已停留在应用首页');
      return;
    }
    handleMenuItemClick(entry, true);
  }, [
    feedback,
    handleMenuItemClick,
    initialEntryNumber,
    menuQuery.data,
    menuQuery.isError,
    menuQuery.isSuccess,
    onInitialEntryConsumed,
  ]);

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
