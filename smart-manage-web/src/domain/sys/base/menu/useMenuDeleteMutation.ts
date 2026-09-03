import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { menuApi } from './api';
import { menuQueryKeys } from './queryKeys';

/** 菜单删除命令及其列表、详情、树缓存一致性规则。 */
export function useMenuDeleteMutation(onSuccess: () => void | Promise<void>) {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  return useCommandMutation({
    mutationFn: (ids: string[]) => Promise.all(ids.map((id) => menuApi.delete(id))),
    onSuccess: async () => {
      queryClient.removeQueries({ queryKey: menuQueryKeys.details() });
      await queryClient.invalidateQueries({ queryKey: menuQueryKeys.all });
      feedback.success('删除成功');
      await onSuccess();
    },
  });
}
