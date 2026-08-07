import { Suspense, useCallback } from 'react';
import { Empty, Result, Spin, Typography } from 'antd';
import { componentRegistry } from '@/domain/common/registry/componentRegistry';
import type { OperationType, PageType } from '@/domain/common/page/types';
import { PageTabTitleProvider } from '@/domain/common/page/PageTabTitleProvider';
import { useWorkbenchStore } from '@/stores/workbench';
import './PageRenderer.css';

interface Props {
  appNumber: string;
  tabKey: string;
  title: string;
  componentKey?: string;
  pageType?: PageType;
  operationType?: OperationType;
  billId?: string;
  context?: Record<string, string>;
  temporary?: boolean;
  /** 当前页签是否激活 */
  active: boolean;
}

/** 页面渲染器 — 通过组件注册表映射真实组件，并精确校验页面类型匹配 */
const PageRenderer = ({
  componentKey,
  appNumber,
  tabKey,
  title,
  pageType,
  operationType,
  billId,
  context,
  temporary,
  active,
}: Props) => {
  const updateContentTabLabel = useWorkbenchStore((state) => state.updateContentTabLabel);
  const setTabTitle = useCallback(
    (nextTitle: string) => updateContentTabLabel(appNumber, tabKey, nextTitle),
    [appNumber, tabKey, updateContentTabLabel],
  );
  if (!componentKey) {
    return (
      <div className="sm-page-renderer-empty">
        <Empty description="页面未配置组件" />
      </div>
    );
  }

  const registration = componentRegistry[componentKey];
  if (!registration) {
    console.error(`[PageRenderer] 页面组件未注册：${componentKey}`);
    return (
      <div className="sm-page-renderer-empty">
        <div className="sm-page-renderer-card">
          <Result
            status="warning"
            title="页面暂不可用"
            subTitle={
              <div className="sm-page-renderer-message">
                <span>当前前端版本未注册该页面，请联系系统管理员检查页面配置。</span>
                <Typography.Text type="secondary" code>
                  {componentKey}
                </Typography.Text>
              </div>
            }
          />
        </div>
      </div>
    );
  }

  // 精确校验：tab 声明的 pageType 必须与注册表一致
  if (pageType && registration.pageType !== pageType) {
    const msg = `页面类型不匹配：componentKey "${componentKey}" 注册为 ${registration.pageType}，但按 ${pageType} 协议打开。请检查菜单 component 配置或 pageRegistration 声明。`;
    console.error(`[PageRenderer] ${msg}`);
    return (
      <div className="sm-page-renderer-empty">
        <Empty description={msg} />
      </div>
    );
  }

  const RegisteredComponent = registration.component;
  return (
    <Suspense
      fallback={
        <div className="sm-page-renderer-empty">
          <Spin />
        </div>
      }
    >
      <PageTabTitleProvider pageType={registration.pageType} setTabTitle={setTabTitle}>
        <RegisteredComponent
          appNumber={appNumber}
          componentKey={componentKey}
          tabKey={tabKey}
          title={title}
          operationType={operationType}
          billId={billId}
          context={context}
          temporary={temporary}
          active={active}
        />
      </PageTabTitleProvider>
    </Suspense>
  );
};

export default PageRenderer;
