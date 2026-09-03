import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';

interface CommandMutationOptions<TData, TVariables> extends Omit<
  UseMutationOptions<TData, Error, TVariables>,
  'onError'
> {
  successMessage?: string | ((variables: TVariables) => string);
}

/** 统一业务命令的成功反馈和错误提示。 */
export function useCommandMutation<TData = unknown, TVariables = void>({
  successMessage,
  onSuccess,
  ...options
}: CommandMutationOptions<TData, TVariables>) {
  const feedback = useOperationFeedback();
  return useMutation<TData, Error, TVariables>({
    ...options,
    onSuccess: async (data, variables, result, context) => {
      try {
        await onSuccess?.(data, variables, result, context);
      } catch {
        feedback.warning('操作已成功，但页面数据刷新失败，请手动刷新');
        return;
      }
      if (successMessage) {
        feedback.success(
          typeof successMessage === 'function' ? successMessage(variables) : successMessage,
        );
      }
    },
    onError: (error) => {
      feedback.fromError(error);
    },
  });
}
