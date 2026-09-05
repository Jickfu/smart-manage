import { useState } from 'react';
import { useWorkbenchStore } from '@/stores/workbench';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import ContentTabsBarView from './ContentTabsBarView';

interface Props {
  appNumber: string;
}

const ContentTabsBar = ({ appNumber }: Props) => {
  const confirmOperation = useOperationConfirm();
  const ws = useWorkbenchStore((state) => state.workspaces[appNumber]);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const removeContentTab = useWorkbenchStore((state) => state.removeContentTab);
  const closeContentTabs = useWorkbenchStore((state) => state.closeContentTabs);
  const [closing, setClosing] = useState(false);

  const contentTabs = ws?.contentTabs ?? [];
  const activeContentTabKey = ws?.activeContentTabKey ?? '';
  const scrollableTabs = contentTabs.filter((tab) => tab.key !== '__home__');

  if (!ws) return null;

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

    void confirmOperation({
      type: 'warning',
      title: '关闭全部页签',
      description: `确定关闭全部 ${allKeys.length} 个页签吗？`,
      confirmText: '确定',
      cancelText: '取消',
      onConfirm: async () => {
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
    <ContentTabsBarView
      contentTabs={contentTabs}
      activeContentTabKey={activeContentTabKey}
      onActivate={(key) => activateContentTab(appNumber, key)}
      onRemove={(key) => {
        void removeContentTab(appNumber, key);
      }}
      onCloseOthers={() => {
        void handleCloseOthers();
      }}
      onCloseAll={handleCloseAll}
      closing={closing}
    />
  );
};

export default ContentTabsBar;
