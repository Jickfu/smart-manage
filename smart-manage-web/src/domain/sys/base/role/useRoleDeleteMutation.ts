import { App } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { permissionQueryKeys } from '@/domain/sys/base/permission/queryKeys';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';

/** 角色删除命令及其缓存一致性规则。 */
export function useRoleDeleteMutation(onSuccess: () => void | Promise<void>) {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => roleApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: roleQueryKeys.details() });
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: roleQueryKeys.all }),
        queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all }),
      ]);
      message.success('删除成功');
      await onSuccess();
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '删除失败'),
  });
}
