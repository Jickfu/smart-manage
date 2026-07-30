import { App } from 'antd';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { menuApi } from './api';
import { menuQueryKeys } from './queryKeys';

/** 菜单删除命令及其列表、详情、树缓存一致性规则。 */
export function useMenuDeleteMutation(onSuccess: () => void | Promise<void>) {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => menuApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: menuQueryKeys.details() });
      await queryClient.invalidateQueries({ queryKey: menuQueryKeys.all });
      message.success('删除成功');
      await onSuccess();
    },
    onError: (error) => message.error(error instanceof Error ? error.message : '删除失败'),
  });
}
