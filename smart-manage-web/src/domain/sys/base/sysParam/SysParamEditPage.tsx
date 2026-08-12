import { useMemo } from 'react';
import { App } from 'antd';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tabKeys';
import EditPage from '@/domain/common/page/EditPage';
import type { EditField } from '@/domain/common/page/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { useAppRefSelector } from '@/domain/sys/base/app/refSelector';
import { sysParamApi } from './api';
import { sysParamAccess } from './permissions';
import { sysParamQueryKeys } from './queryKeys';

const SysParamEditPage = (props: PageComponentProps) => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const { appNumber, tabKey, operationType, billId } = props;
  const isAddNew = operationType === OperationType.ADDNEW;
  const replaceContentTab = useWorkbenchStore((state) => state.replaceContentTab);
  const activateContentTab = useWorkbenchStore((state) => state.activateContentTab);
  const appRefSelector = useAppRefSelector();
  const detailQuery = useQuery({
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
        label: '所属应用',
        dataIndex: 'application',
        type: 'ref-selector',
        disabled: detail?.isSystem,
        placeholder: '留空表示全局参数',
        refSelector: appRefSelector,
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
    [appRefSelector, detail?.isSystem],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? {
            number: detail.number,
            name: detail.name,
            value: detail.value ?? '',
            description: detail.description ?? '',
            application: detail.appId
              ? { id: detail.appId, name: detail.appName ?? detail.appId }
              : null,
          }
        : {},
    [detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const application = values.application as { id?: string } | null;
      const savedId = await sysParamApi.save({
        id: billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        value: String(values.value ?? ''),
        description: String(values.description ?? ''),
        appId: application?.id,
      });
      if (isAddNew) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        replaceContentTab(appNumber, tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        activateContentTab(appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: sysParamQueryKeys.all });
      message.success(isAddNew ? '新增成功' : '保存成功');
    },
  });
  return (
    <EditPage
      access={sysParamAccess}
      title="系统参数"
      fields={fields}
      initialValues={initialValues}
      operationType={operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber, tabKey }}
      loading={detailQuery.isLoading}
      error={detailQuery.error as Error | null}
      onRetry={() => detailQuery.refetch()}
      onSave={saveMutation.mutateAsync}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(appNumber, tabKey)}
    />
  );
};

export default SysParamEditPage;
