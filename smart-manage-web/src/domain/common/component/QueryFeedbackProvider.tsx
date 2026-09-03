import { useEffect, useState, type ReactNode } from 'react';
import { QueryClientProvider } from '@tanstack/react-query';
import { createQueryFeedbackRuntime } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from './useOperationFeedback';

/** AntApp 下的反馈桥接；QueryClient 生命周期不随主题或普通渲染重新创建。 */
export function QueryFeedbackProvider({ children }: { children: ReactNode }) {
  const feedback = useOperationFeedback();
  const [runtime] = useState(createQueryFeedbackRuntime);
  useEffect(
    () =>
      runtime.connect({
        show: (error, key) =>
          feedback.fromError(error, '数据加载失败，请稍后重试', { key, autoClose: false }),
        close: feedback.close,
      }),
    [feedback, runtime],
  );
  return <QueryClientProvider client={runtime.queryClient}>{children}</QueryClientProvider>;
}
