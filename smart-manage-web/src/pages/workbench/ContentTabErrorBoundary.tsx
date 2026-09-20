import { Component, useCallback } from 'react';
import type { ErrorInfo, ReactNode } from 'react';
import { Button, Result } from 'antd';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useWorkbenchStore } from '@/stores/workbench';

interface BoundaryProps {
  children: ReactNode;
  onError: () => void;
  onRecovered: () => void;
  fallback: ReactNode;
}

interface BoundaryState {
  failed: boolean;
}

class ContentTabBoundary extends Component<BoundaryProps, BoundaryState> {
  state: BoundaryState = { failed: false };

  static getDerivedStateFromError(): BoundaryState {
    return { failed: true };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('[ContentTabErrorBoundary] 页签渲染失败', error, errorInfo);
    this.props.onError();
  }

  componentWillUnmount() {
    if (this.state.failed) this.props.onRecovered();
  }

  render() {
    return this.state.failed ? this.props.fallback : this.props.children;
  }
}

/** 将渲染故障限制在单个页签，并在子页面守卫已清理后保留显式风险确认。 */
export default function ContentTabErrorBoundary({
  appNumber,
  tabKey,
  closable,
  children,
}: {
  appNumber: string;
  tabKey: string;
  closable: boolean;
  children: ReactNode;
}) {
  const confirmOperation = useOperationConfirm();
  const registerFailedClose = useWorkbenchStore((state) => state.registerFailedClose);
  const unregisterFailedClose = useWorkbenchStore((state) => state.unregisterFailedClose);
  const removeContentTab = useWorkbenchStore((state) => state.removeContentTab);
  const checkAllDirty = useWorkbenchStore((state) => state.checkAllDirty);

  const confirmFailedTabClose = useCallback(
    () =>
      confirmOperation({
        type: 'warning',
        title: '关闭故障页面',
        description:
          '当前页面发生异常，本页未保存内容可能已经丢失或无法恢复。关闭不会撤销已经发出的操作，是否继续关闭？',
        confirmText: '继续关闭',
        cancelText: '取消',
      }),
    [confirmOperation],
  );

  const handleError = useCallback(() => {
    registerFailedClose(appNumber, tabKey, confirmFailedTabClose);
  }, [appNumber, confirmFailedTabClose, registerFailedClose, tabKey]);

  const handleRecovered = useCallback(() => {
    unregisterFailedClose(appNumber, tabKey);
  }, [appNumber, tabKey, unregisterFailedClose]);

  const handleReload = async () => {
    if (await checkAllDirty()) window.location.reload();
  };

  return (
    <ContentTabBoundary
      onError={handleError}
      onRecovered={handleRecovered}
      fallback={
        <Result
          status="error"
          title="当前页面发生异常"
          subTitle="本页未保存内容可能无法恢复，其他页签仍可继续使用。"
          extra={[
            ...(closable
              ? [
                  <Button
                    key="close"
                    type="primary"
                    onClick={() => void removeContentTab(appNumber, tabKey)}
                  >
                    关闭当前页签
                  </Button>,
                ]
              : []),
            <Button key="reload" onClick={() => void handleReload()}>
              刷新整个系统页面
            </Button>,
          ]}
        />
      }
    >
      {children}
    </ContentTabBoundary>
  );
}
