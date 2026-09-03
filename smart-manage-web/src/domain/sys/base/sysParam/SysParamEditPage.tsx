import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useEditTabLifecycle } from '@/domain/common/page/edit/useEditTabLifecycle';
import {
  getEditSavePostCommitFeedback,
  runEditSavePostCommit,
} from '@/domain/common/page/edit/editSavePostCommit';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useFeatureRefSelector } from '@/domain/sys/base/feature/refSelector';
import { sysParamApi } from './api';
import { sysParamAccess } from './permissions';
import { sysParamQueryKeys } from './queryKeys';

const SysParamEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const { isAddNew, promoteToPersistedTab, exit } = useEditTabLifecycle(props);
  const featureRefSelector = useFeatureRefSelector();
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: sysParamQueryKeys.detail(billId),
    queryFn: () => sysParamApi.detail(billId!),
    enabled: Boolean(billId),
  });
  const detail = detailQuery.data;
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '参数编码',
        dataIndex: 'number',
        type: 'text',
        disabled: detail?.isSystem,
        rules: [{ required: true, message: '参数编码不能为空' }],
      },
      {
        label: '参数名称',
        dataIndex: 'name',
        type: 'text',
        disabled: detail?.isSystem,
        rules: [{ required: true, message: '参数名称不能为空' }],
      },
      {
        label: '所属功能',
        dataIndex: 'feature',
        type: 'ref-selector',
        disabled: detail?.isSystem,
        placeholder: '留空表示全局参数',
        refSelector: featureRefSelector,
      },
      { label: '参数值', dataIndex: 'value', type: 'textarea', fullWidth: true },
      {
        label: '描述',
        dataIndex: 'description',
        type: 'textarea',
        fullWidth: true,
        disabled: detail?.isSystem,
      },
    ],
    [detail?.isSystem, featureRefSelector],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? {
            number: detail.number,
            name: detail.name,
            value: detail.value ?? '',
            description: detail.description ?? '',
            feature: detail.feature ?? null,
          }
        : {},
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const feature = values.feature as { id?: string } | null;
      const savedId = await sysParamApi.save({
        id: billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        value: String(values.value ?? ''),
        description: String(values.description ?? ''),
        featureId: feature?.id,
      });
      return savedId;
    },
    onSuccess: async (savedId) => {
      const result = await runEditSavePostCommit({
        syncTab: () => promoteToPersistedTab(savedId),
        refreshCache: () =>
          queryClient.invalidateQueries(
            { queryKey: sysParamQueryKeys.all },
            { throwOnError: true },
          ),
      });
      const resultFeedback = getEditSavePostCommitFeedback(result, isAddNew);
      feedback[resultFeedback.type](resultFeedback.message);
    },
  });
  return (
    <EditPage
      access={sysParamAccess}
      title="系统参数"
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
      onExit={exit}
    />
  );
};

export default SysParamEditPage;
