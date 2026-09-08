import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import ModalEditPage from '@/domain/common/page/edit/ModalEditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { weakPasswordApi } from './api';
import { weakPasswordAccess } from './permissions';
import { weakPasswordQueryKeys } from './queryKeys';

interface Props {
  open: boolean;
  wordId?: string;
  onClose: () => void;
}

const WeakPasswordEditPage = ({ open, wordId, onClose }: Props) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const isAddNew = !wordId;
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: weakPasswordQueryKeys.detail(wordId),
    queryFn: () => weakPasswordApi.detail(wordId!),
    enabled: Boolean(open && wordId),
    staleTime: 0,
  });
  const detail = detailQuery.data;
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '弱口令',
        dataIndex: 'word',
        type: 'text',
        fullWidth: true,
        tooltip: '完整词条匹配，忽略大小写，保存后生效；请勿录入正在使用的真实密码。',
        rules: [
          { required: true, message: '弱口令不能为空' },
          {
            validator: (_rule: unknown, value?: string) =>
              value && value.trim() && Array.from(value).length <= 64
                ? Promise.resolve()
                : Promise.reject(new Error('弱口令须为1～64个字符，不能全为空白')),
          },
        ],
      },
      {
        label: '描述',
        dataIndex: 'description',
        type: 'textarea',
        fullWidth: true,
        rules: [{ max: 500, message: '描述不能超过500个字符' }],
      },
    ],
    [],
  );
  const initialValues = useMemo(
    () => (detail ? { word: detail.word, description: detail.description ?? '' } : {}),
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const savedId = await weakPasswordApi.save({
        id: wordId,
        version: detail?.version,
        word: String(values.word ?? ''),
        description: String(values.description ?? ''),
      });
      return savedId;
    },
    onSuccess: async () => {
      // 保存已提交即关闭弹窗，刷新失败由查询反馈负责，避免用户重复提交。
      onClose();
      feedback.success(isAddNew ? '新增成功' : '保存成功');
      await queryClient.invalidateQueries({ queryKey: weakPasswordQueryKeys.all });
    },
  });
  return (
    <ModalEditPage
      access={weakPasswordAccess}
      title={isAddNew ? '新增弱口令' : '编辑弱口令'}
      open={open}
      onClose={onClose}
      fields={fields}
      initialValues={initialValues}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => {
        if (open && wordId) void detailQuery.refetch();
      }}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
    />
  );
};

export default WeakPasswordEditPage;
