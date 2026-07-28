import type { ReactNode } from 'react';
import { Button, Result, Spin } from 'antd';
import './EditPage.css';

interface EditPageShellProps {
  loading: boolean;
  error?: Error | null;
  onRetry?: () => void;
  actions: ReactNode;
  children: ReactNode;
}

/** 编辑页壳层：只负责布局、加载、错误和按钮区域，不感知业务命令与单据状态。 */
export function EditPageShell({ loading, error, onRetry, actions, children }: EditPageShellProps) {
  if (error) {
    return (
      <section className="sm-common-page sm-edit-page">
        <Result
          status="error"
          title="加载失败"
          subTitle={error.message || '请检查网络连接后重试'}
          extra={
            onRetry && (
              <Button type="primary" onClick={onRetry}>
                重试
              </Button>
            )
          }
        />
      </section>
    );
  }

  return (
    <section className="sm-common-page sm-edit-page">
      <div className="sm-edit-header">
        <div className="sm-edit-header-actions">{actions}</div>
      </div>
      <div className="sm-edit-body">
        <Spin spinning={loading}>{children}</Spin>
      </div>
    </section>
  );
}
