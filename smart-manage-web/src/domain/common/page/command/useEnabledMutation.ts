import { useCommandMutation } from './useCommandMutation';

interface EnabledCommand {
  ids: string[];
  enabled: boolean;
}

/** 列表批量启停命令，成功后由调用方刷新数据并清空选择。 */
export function useEnabledMutation(
  command: (ids: string[], enabled: boolean) => Promise<unknown>,
  onSuccess: () => void | Promise<void>,
) {
  return useCommandMutation({
    mutationFn: ({ ids, enabled }: EnabledCommand) => command(ids, enabled),
    successMessage: (variables) => (variables.enabled ? '启用成功' : '禁用成功'),
    onSuccess: async (_data, _variables) => {
      await onSuccess();
    },
  });
}
