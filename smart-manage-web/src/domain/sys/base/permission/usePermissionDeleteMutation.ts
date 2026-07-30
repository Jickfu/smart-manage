import { App } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { permissionApi } from './api';
import { permissionQueryKeys } from './queryKeys';

/** 权限删除命令及其缓存一致性规则。 */
export function usePermissionDeleteMutation(onSuccess: () => void | Promise<void>) {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => permissionApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: permissionQueryKeys.details() });
      await queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all });
      message.success('删除成功');
      await onSuccess();
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '删除失败'),
  });
}
