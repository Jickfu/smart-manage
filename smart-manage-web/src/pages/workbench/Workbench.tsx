import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { memo, useCallback, useEffect, useRef } from 'react';
import { Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { menuQueryKeys } from '@/domain/sys/base/menu/queryKeys';
import {
  retainedPageLimitMessage,
  retainedPageWarningMessage,
  useWorkbenchStore,
} from '@/stores/workbench';
import { getUserMenusByAppNumber } from '@/domain/sys/base/menu/api';
import AppSidebar from './AppSidebar';
import ContentTabsBar from './ContentTabsBar';
import PageRenderer from './PageRenderer';
import ApplicationHome from './ApplicationHome';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { MenuVO } from '@/types/api';
import ExternalLinkFrame from './ExternalLinkFrame';
import { findMenuEntry, resolveMenuAction } from './menuNavigation';
import { isContentPageActive } from './pageActivity';
import ContentTabErrorBoundary from './ContentTabErrorBoundary';
import './Workbench.css';

interface Props {
  appNumber: string;
  /** 所属顶部应用当前是否可见。 */
  appActive: boolean;
  initialEntryNumber?: string;
  onInitialEntryConsumed: () => void;
}

const Workbench = ({ appNumber, appActive, initialEntryNumber, onInitialEntryConsumed }: Props) => {
  const feedback = useOperationFeedback();
  const startupEntryConsumed = useRef(false);
  const ws = useWorkbenchStore((s) => s.workspaces[appNumber]);
  const capacityNotice = useWorkbenchStore((s) => s.capacityNotice);
  const consumeCapacityNotice = useWorkbenchStore((s) => s.consumeCapacityNotice);
  const openListTab = useWorkbenchStore((s) => s.openListTab);
  const openCustomTab = useWorkbenchStore((s) => s.openCustomTab);
  const openExternalLinkTab = useWorkbenchStore((s) => s.openExternalLinkTab);

  useEffect(() => {
    if (!capacityNotice || capacityNotice.appNumber !== appNumber) return;
    feedback.warning(retainedPageWarningMessage(), {
      key: `workbench-capacity-${capacityNotice.revision}`,
    });
    consumeCapacityNotice(capacityNotice.revision);
  }, [appNumber, capacityNotice, consumeCapacityNotice, feedback]);

  const menuQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
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
          if (
            openExternalLinkTab(appNumber, action.menuId, action.title, action.externalUrl) ===
            'capacity-exceeded'
          ) {
            feedback.warning(retainedPageLimitMessage());
          }
          return;
        }
        if (componentRegistry[action.componentKey]?.pageType === 'CUSTOM') {
          if (openCustomTab(appNumber, action.componentKey) === 'capacity-exceeded') {
            feedback.warning(retainedPageLimitMessage());
          }
        } else {
          if (openListTab(appNumber, action.componentKey) === 'capacity-exceeded') {
            feedback.warning(retainedPageLimitMessage());
          }
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
      // 本地菜单错误区负责反馈；重试成功后仍可解析原始入口，不额外弹出重复警告。
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
        error={getBlockingQueryError(menuQuery)}
        onRetry={() => void menuQuery.refetch()}
        onItemClick={handleMenuItemClick}
      />
      <div className="sm-workspace-body">
        <ContentTabsBar appNumber={appNumber} />
        <Spin spinning={menuQuery.isLoading}>
          <ol className="sm-workspace-content">
            {ws.contentTabs.map((tab) => {
              const contentTabActive = ws.activeContentTabKey === tab.key;
              const effectiveActive = isContentPageActive(appActive, contentTabActive);
              return (
                <li
                  key={tab.key}
                  className={`sm-content-pane ${contentTabActive ? 'sm-content-pane--active' : ''}`}
                >
                  <ContentTabErrorBoundary
                    appNumber={appNumber}
                    tabKey={tab.key}
                    closable={tab.closable}
                  >
                    {tab.key === '__home__' ? (
                      <ApplicationHome appNumber={appNumber} />
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
                        active={effectiveActive}
                      />
                    )}
                  </ContentTabErrorBoundary>
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
