import type { ReactNode } from 'react';
import { Spin } from 'antd';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import './pageLayout.css';
import { usePageTabTitle } from './tab/usePageTabTitle';

interface EditPageShellProps {
  title: string;
  loading: boolean;
  error?: Error | null;
  onRetry?: () => void;
  actions: ReactNode;
  children: ReactNode;
  /** 可选的独立业务侧栏，与主表单的滚动和禁用状态隔离。 */
  sidePanel?: ReactNode;
  sidePanelLabel?: string;
}

/** 编辑页壳层：只负责布局、加载、错误和按钮区域，不感知业务命令与单据状态。 */
export function EditPageShell({
  title,
  loading,
  error,
  onRetry,
  actions,
  children,
  sidePanel,
  sidePanelLabel = '补充信息',
}: EditPageShellProps) {
  usePageTabTitle(title);
  return (
    <section className="sm-common-page sm-edit-page">
      {error && <RequestErrorState error={error} onRetry={onRetry} />}
      {actions && (
        <div className="sm-edit-header" hidden={Boolean(error)} inert={Boolean(error)}>
          <div className="sm-edit-header-actions">{actions}</div>
        </div>
      )}
      <div
        className={`sm-edit-body${sidePanel ? ' sm-edit-body--split' : ''}`}
        hidden={Boolean(error)}
        inert={Boolean(error)}
      >
        <Spin spinning={loading}>
          {sidePanel ? (
            <div className="sm-edit-columns">
              <div className="sm-edit-main">{children}</div>
              <aside className="sm-edit-side-panel" aria-label={sidePanelLabel}>
                {sidePanel}
              </aside>
            </div>
          ) : (
            children
          )}
        </Spin>
      </div>
    </section>
  );
}
