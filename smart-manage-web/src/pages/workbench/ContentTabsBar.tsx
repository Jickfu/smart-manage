import { useState } from 'react';
import { App } from 'antd';
import { CloseCircleOutlined, CloseSquareOutlined, HomeOutlined } from '@ant-design/icons';
import { Tooltip } from 'antd';
import { useWorkbenchStore } from '@/stores/workbench';
import './ContentTabsBar.css';

interface Props {
  appNumber: string;
}

const ContentTabsBar = ({ appNumber }: Props) => {
  const { modal } = App.useApp();
  const ws = useWorkbenchStore((s) => s.workspaces[appNumber]);
  const activateContentTab = useWorkbenchStore((s) => s.activateContentTab);
  const removeContentTab = useWorkbenchStore((s) => s.removeContentTab);
  const closeContentTabs = useWorkbenchStore((s) => s.closeContentTabs);
  const [closing, setClosing] = useState(false);

  if (!ws) return null;

  const { contentTabs, activeContentTabKey } = ws;

  const handleTabClick = (key: string) => {
    activateContentTab(appNumber, key);
  };

  const handleRemove = (event: React.MouseEvent, key: string) => {
    event.stopPropagation();
    removeContentTab(appNumber, key);
  };

  const handleCloseOthers = async () => {
    if (closing) return;
    const othersToClose = contentTabs
      .filter((t) => t.closable && t.key !== activeContentTabKey)
      .map((t) => t.key);
    if (othersToClose.length === 0) return;

    setClosing(true);
    try {
      await closeContentTabs(appNumber, othersToClose);
    } finally {
      setClosing(false);
    }
  };

  const handleCloseAll = () => {
    const allKeys = contentTabs.filter((t) => t.closable).map((t) => t.key);
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

  return (
    <div className="sm-content-tabs-bar">
      <div className="sm-content-tabs" role="tablist" aria-label="内容页签">
        {contentTabs.map((tab) => {
          const isHome = tab.key === '__home__';
          const isActive = activeContentTabKey === tab.key;
          return (
            <div
              key={tab.key}
              role="tab"
              tabIndex={0}
              aria-selected={isActive}
              className={`sm-content-tab ${isHome ? 'sm-content-tab-home' : ''} ${isActive ? 'sm-content-tab--active' : ''}`}
              onClick={() => handleTabClick(tab.key)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleTabClick(tab.key);
                }
              }}
            >
              {isHome ? (
                <HomeOutlined />
              ) : (
                <>
                  <span>{tab.label}</span>
                  {tab.closable && (
                    <button
                      className="sm-content-tab-close"
                      onClick={(event) => handleRemove(event, tab.key)}
                      aria-label={`关闭 ${tab.label}`}
                    >
                      ✕
                    </button>
                  )}
                </>
              )}
            </div>
          );
        })}
      </div>
      <div className="sm-content-tabs-actions">
        <Tooltip title="关闭其他页签" placement="bottomRight" autoAdjustOverflow={false}>
          <button
            className="sm-content-tabs-action-btn"
            onClick={handleCloseOthers}
            aria-label="关闭其他页签"
            disabled={closing}
          >
            <CloseSquareOutlined />
          </button>
        </Tooltip>
        <Tooltip title="关闭全部页签" placement="bottomRight" autoAdjustOverflow={false}>
          <button
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
