import { App } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { roleQueryKeys } from '@/domain/sys/base/role/queryKeys';
import { userApi } from './api';
import { userQueryKeys } from './queryKeys';

/** 用户删除命令及其缓存一致性规则。 */
export function useUserDeleteMutation(onSuccess: () => void | Promise<void>) {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => userApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: userQueryKeys.details() });
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: userQueryKeys.all }),
        queryClient.invalidateQueries({ queryKey: roleQueryKeys.all }),
      ]);
      message.success('删除成功');
      await onSuccess();
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '删除失败'),
  });
}
