import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { permissionApi } from './api';
import { permissionQueryKeys } from './queryKeys';

/** 权限删除命令及其缓存一致性规则。 */
export function usePermissionDeleteMutation(onSuccess: () => void | Promise<void>) {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  return useCommandMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => permissionApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: permissionQueryKeys.details() });
      await queryClient.invalidateQueries({ queryKey: permissionQueryKeys.all });
      feedback.success('删除成功');
      await onSuccess();
    },
  });
}
