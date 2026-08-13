import { useState } from 'react';
import { App, Tooltip } from 'antd';
import { CloseCircleOutlined, CloseSquareOutlined, HomeOutlined } from '@ant-design/icons';
import { useHorizontalTabScroll } from '@/hooks/useHorizontalTabScroll';
import { useWorkbenchStore } from '@/stores/workbench';
import './ContentTabsBar.css';

interface Props {
  appNumber: string;
}

const ContentTabsBar = ({ appNumber }: Props) => {
  const { modal } = App.useApp();
  const ws = useWorkbenchStore((state) => state.workspaces[appNumber]);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const removeContentTab = useWorkbenchStore((state) => state.removeContentTab);
  const closeContentTabs = useWorkbenchStore((state) => state.closeContentTabs);
  const [closing, setClosing] = useState(false);

  const contentTabs = ws?.contentTabs ?? [];
  const activeContentTabKey = ws?.activeContentTabKey ?? '';
  const homeTab = contentTabs.find((tab) => tab.key === '__home__');
  const scrollableTabs = contentTabs.filter((tab) => tab.key !== '__home__');
  const { viewportRef, activeTabRef, canScrollLeft, canScrollRight, scroll } =
    useHorizontalTabScroll(activeContentTabKey, scrollableTabs.length);

  if (!ws) return null;

  const handleTabClick = (key: string) => {
    activateContentTab(appNumber, key);
  };

  const handleRemove = (event: React.MouseEvent, key: string) => {
    event.stopPropagation();
    void removeContentTab(appNumber, key);
  };

  const handleCloseOthers = async () => {
    if (closing) return;
    const othersToClose = scrollableTabs
      .filter((tab) => tab.key !== activeContentTabKey)
      .map((tab) => tab.key);
    if (othersToClose.length === 0) return;

    setClosing(true);
    try {
      await closeContentTabs(appNumber, othersToClose);
    } finally {
      setClosing(false);
    }
  };

  const handleCloseAll = () => {
    const allKeys = scrollableTabs.map((tab) => tab.key);
    if (allKeys.length === 0) return;

    modal.confirm({
      title: '关闭全部页签',
      content: `确定关闭全部 ${allKeys.length} 个页签吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        setClosing(true);
        try {
          await closeContentTabs(appNumber, allKeys);
        } finally {
          setClosing(false);
        }
      },
    });
  };

  const renderTab = (tab: (typeof contentTabs)[number], isActive: boolean, className: string) => (
    <div
      key={tab.key}
      ref={isActive && tab.key !== '__home__' ? activeTabRef : undefined}
      role="tab"
      tabIndex={0}
      aria-selected={isActive}
      className={className}
      onClick={() => handleTabClick(tab.key)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          handleTabClick(tab.key);
        }
      }}
    >
      {tab.key === '__home__' ? (
        <HomeOutlined />
      ) : (
        <>
          <span>{tab.label}</span>
          <button
            type="button"
            className="sm-content-tab-close"
            onClick={(event) => handleRemove(event, tab.key)}
            aria-label={`关闭 ${tab.label}`}
          >
            ×
          </button>
        </>
      )}
    </div>
  );

  return (
    <div className="sm-content-tabs-bar" role="tablist" aria-label="内容页签">
      {homeTab &&
        renderTab(
          homeTab,
          activeContentTabKey === homeTab.key,
          `sm-content-tab sm-content-tab-home ${activeContentTabKey === homeTab.key ? 'sm-content-tab--active' : ''}`,
        )}
      <button
        type="button"
        className="sm-content-tabs-scroll-btn"
        aria-label="向左移动页签"
        disabled={!canScrollLeft}
        onClick={() => scroll(-1)}
      >
        &lt;
      </button>
      <div ref={viewportRef} className="sm-content-tabs-viewport">
        <div className="sm-content-tabs">
          {scrollableTabs.map((tab) => {
            const isActive = activeContentTabKey === tab.key;
            return renderTab(
              tab,
              isActive,
              `sm-content-tab ${isActive ? 'sm-content-tab--active' : ''}`,
            );
          })}
        </div>
      </div>
      <button
        type="button"
        className="sm-content-tabs-scroll-btn"
        aria-label="向右移动页签"
        disabled={!canScrollRight}
        onClick={() => scroll(1)}
      >
        &gt;
      </button>
      <div className="sm-content-tabs-actions">
        <Tooltip title="关闭其他页签" placement="bottomRight" autoAdjustOverflow={false}>
          <button
            type="button"
            className="sm-content-tabs-action-btn"
            onClick={() => void handleCloseOthers()}
            aria-label="关闭其他页签"
            disabled={closing}
          >
            <CloseSquareOutlined />
          </button>
        </Tooltip>
        <Tooltip title="关闭全部页签" placement="bottomRight" autoAdjustOverflow={false}>
          <button
            type="button"
            className="sm-content-tabs-action-btn"
            onClick={handleCloseAll}
            aria-label="关闭全部页签"
            disabled={closing}
          >
            <CloseCircleOutlined />
          </button>
        </Tooltip>
      </div>
    </div>
  );
};

export default ContentTabsBar;
