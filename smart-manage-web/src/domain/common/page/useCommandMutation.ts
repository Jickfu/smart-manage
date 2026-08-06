import { App } from 'antd';
import { useMutation } from '@tanstack/react-query';
import type { UseMutationOptions } from '@tanstack/react-query';
import { ApiError } from '@/api/ApiError';

const DATA_CONFLICT_CODE = 100409;

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
  const { message } = App.useApp();
  return useMutation<TData, Error, TVariables>({
    ...options,
    onSuccess: async (data, variables, result, context) => {
      try {
        await onSuccess?.(data, variables, result, context);
      } catch {
        message.warning('操作已成功，但页面数据刷新失败，请手动刷新');
        return;
      }
      if (successMessage) {
        message.success(
          typeof successMessage === 'function' ? successMessage(variables) : successMessage,
        );
      }
    },
    onError: (error) => {
      if (error instanceof ApiError && error.code === DATA_CONFLICT_CODE) {
        message.error('数据已被其他请求修改，请刷新后重试');
        return;
      }
      message.error(error.message || '操作失败');
    },
  });
}
