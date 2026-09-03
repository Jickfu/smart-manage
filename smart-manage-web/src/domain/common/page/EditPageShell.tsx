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
}

/** 编辑页壳层：只负责布局、加载、错误和按钮区域，不感知业务命令与单据状态。 */
export function EditPageShell({
  title,
  loading,
  error,
  onRetry,
  actions,
  children,
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
      <div className="sm-edit-body" hidden={Boolean(error)} inert={Boolean(error)}>
        <Spin spinning={loading}>{children}</Spin>
      </div>
    </section>
  );
}
