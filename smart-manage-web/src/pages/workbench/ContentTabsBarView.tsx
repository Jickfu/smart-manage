import { Tooltip } from 'antd';
import { CloseCircleOutlined, CloseSquareOutlined, HomeOutlined } from '@ant-design/icons';
import { useHorizontalTabScroll } from '@/hooks/useHorizontalTabScroll';
import './ContentTabsBar.css';

export interface ContentTabViewItem {
  key: string;
  label: string;
  closable: boolean;
}

/** 应用工作台和内置消息中心共享页签呈现，状态及关闭策略由各自调用方维护。 */
export default function ContentTabsBarView({
  contentTabs,
  activeContentTabKey,
  onActivate,
  onRemove,
  onCloseOthers,
  onCloseAll,
  closing = false,
}: {
  contentTabs: ContentTabViewItem[];
  activeContentTabKey: string;
  onActivate: (key: string) => void;
  onRemove: (key: string) => void;
  onCloseOthers: () => void;
  onCloseAll: () => void;
  closing?: boolean;
}) {
  const homeTab = contentTabs.find((tab) => tab.key === '__home__');
  const scrollableTabs = contentTabs.filter((tab) => tab.key !== '__home__');
  const { viewportRef, activeTabRef, overflowing, canScrollLeft, canScrollRight, scroll } =
    useHorizontalTabScroll(activeContentTabKey, scrollableTabs.length);
  const handleRemove = (event: React.MouseEvent, key: string) => {
    event.stopPropagation();
    onRemove(key);
  };
  const renderTab = (tab: ContentTabViewItem, isActive: boolean, className: string) => (
    <div
      key={tab.key}
      ref={isActive && tab.key !== '__home__' ? activeTabRef : undefined}
      role="tab"
      aria-label={tab.key === '__home__' ? tab.label : undefined}
      tabIndex={0}
      aria-selected={isActive}
      className={className}
      onClick={() => onActivate(tab.key)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onActivate(tab.key);
        }
      }}
    >
      {tab.key === '__home__' ? (
        <HomeOutlined />
      ) : (
        <>
          <span>{tab.label}</span>
          {tab.closable && (
            <button
              type="button"
              className="sm-content-tab-close"
              onClick={(event) => handleRemove(event, tab.key)}
              aria-label={`关闭 ${tab.label}`}
            >
              ×
            </button>
          )}
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
      {overflowing && (
        <button
          type="button"
          className="sm-content-tabs-scroll-btn"
          aria-label="向左移动页签"
          disabled={!canScrollLeft}
          onClick={() => scroll(-1)}
        >
          &lt;
        </button>
      )}
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
      {overflowing && (
        <button
          type="button"
          className="sm-content-tabs-scroll-btn"
          aria-label="向右移动页签"
          disabled={!canScrollRight}
          onClick={() => scroll(1)}
        >
          &gt;
        </button>
      )}
      <div className="sm-content-tabs-actions">
        <Tooltip title="关闭其他页签" placement="bottomRight" autoAdjustOverflow={false}>
          <button
            type="button"
            className="sm-content-tabs-action-btn"
            onClick={() => onCloseOthers()}
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
            onClick={onCloseAll}
            aria-label="关闭全部页签"
            disabled={closing}
          >
            <CloseCircleOutlined />
          </button>
        </Tooltip>
      </div>
    </div>
  );
}
