import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { permissionQueryKeys } from '@/domain/sys/base/permission/queryKeys';
import { roleApi } from './api';
import { roleQueryKeys } from './queryKeys';

/** 角色删除命令及其缓存一致性规则。 */
export function useRoleDeleteMutation(onSuccess: () => void | Promise<void>) {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  return useCommandMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => roleApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: roleQueryKeys.details() });
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: roleQueryKeys.all }),
        queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all }),
      ]);
      feedback.success('删除成功');
      await onSuccess();
    },
  });
}
