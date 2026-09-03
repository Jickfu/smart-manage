import { Button, Result, Typography } from 'antd';
import { getErrorPresentation } from '@/api/errorPresentation';
import './RequestErrorState.css';

/** 页面与顶部反馈共享安全说明/诊断展示，不自动展开附加数据或异常堆栈。 */
export function RequestErrorDescription({
  error,
  fallbackMessage,
}: {
  error: unknown;
  fallbackMessage?: string;
}) {
  const presentation = getErrorPresentation(error, fallbackMessage);
  return (
    <>
      {presentation.message}
      {presentation.traceId && (
        <Typography.Text
          className="sm-request-error-trace"
          type="secondary"
          copyable={{ text: presentation.traceId }}
        >
          诊断 ID：{presentation.traceId}
        </Typography.Text>
      )}
    </>
  );
}

/** 只复用一致的区域 Result 形态，不承载页面布局或 Query 生命周期。 */
export function RequestErrorState({
  error,
  onRetry,
  title = '加载失败',
}: {
  error: unknown;
  onRetry?: () => void;
  title?: string;
}) {
  const presentation = getErrorPresentation(error, '数据加载失败，请稍后重试');
  if (presentation.suppressed) return null;
  return (
    <Result
      status={presentation.type}
      title={title}
      subTitle={
        <RequestErrorDescription error={error} fallbackMessage="数据加载失败，请稍后重试" />
      }
      extra={
        onRetry && (
          <Button type="primary" onClick={onRetry}>
            重试
          </Button>
        )
      }
    />
  );
}
