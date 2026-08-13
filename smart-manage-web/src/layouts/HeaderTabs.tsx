import { useHorizontalTabScroll } from '@/hooks/useHorizontalTabScroll';
import type { HeaderTabItem } from '@/stores/headerTabs';

interface Props {
  tabs: HeaderTabItem[];
  activeKey: string;
  onActivate: (key: string) => void;
  onRemove: (event: React.MouseEvent, key: string) => void;
}

const HeaderTabs = ({ tabs, activeKey, onActivate, onRemove }: Props) => {
  const fixedTabs = tabs.filter((tab) => !tab.closable);
  const appTabs = tabs.filter((tab) => tab.closable);
  const { viewportRef, activeTabRef, canScrollLeft, canScrollRight, scroll } =
    useHorizontalTabScroll(activeKey, appTabs.length);

  const renderTab = (tab: HeaderTabItem, activeTab: boolean) => (
    <div
      key={tab.key}
      ref={activeTab && tab.closable ? activeTabRef : undefined}
      role="tab"
      tabIndex={0}
      aria-selected={activeTab}
      className={`sm-header-tab ${activeTab ? 'sm-header-tab--active' : ''}`}
      onClick={() => onActivate(tab.key)}
      onKeyDown={(event) => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          onActivate(tab.key);
        }
      }}
    >
      <span>{tab.label}</span>
      {tab.closable && (
        <div className="sm-header-tab-operate">
          <button
            type="button"
            className="sm-header-tab-operate-close"
            onClick={(event) => onRemove(event, tab.key)}
            aria-label={`关闭 ${tab.label}`}
          >
            ×
          </button>
        </div>
      )}
    </div>
  );

  return (
    <nav className="sm-header-tabs" role="tablist" aria-label="应用切换">
      <div className="sm-header-fixed-tabs">
        {fixedTabs.map((tab) => renderTab(tab, activeKey === tab.key))}
      </div>
      <button
        type="button"
        className="sm-header-tabs-scroll-btn"
        aria-label="向左移动应用页签"
        disabled={!canScrollLeft}
        onClick={() => scroll(-1)}
      >
        &lt;
      </button>
      <div ref={viewportRef} className="sm-header-tabs-viewport">
        <div className="sm-header-scrollable-tabs">
          {appTabs.map((tab) => renderTab(tab, activeKey === tab.key))}
        </div>
      </div>
      <button
        type="button"
        className="sm-header-tabs-scroll-btn"
        aria-label="向右移动应用页签"
        disabled={!canScrollRight}
        onClick={() => scroll(1)}
      >
        &gt;
      </button>
    </nav>
  );
};

export default HeaderTabs;
