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
import { useOrgRefSelector } from '@/domain/sys/base/org/refSelector/useOrgRefSelector';
import { orgApi } from '@/domain/sys/base/org/api';
import { orgQueryKeys } from '@/domain/sys/base/org/queryKeys';
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
  const isRegistrationPolicy =
    detail?.number === 'SELF_REGISTRATION_POLICY' && detail.isSystem === true;
  const policy = isRegistrationPolicy
    ? (detail.value ?? '').match(/^ORGANIZATION:([0-9]+):(.+)$/s)
    : null;
  const registrationOrgId = policy?.[1];
  const registrationPosition = policy?.[2];
  const registrationOrgSelector = useOrgRefSelector(undefined, isRegistrationPolicy);
  const registrationOrgQuery = useQuery({
    queryKey: orgQueryKeys.detail(registrationOrgId ?? null),
    queryFn: () => orgApi.detail(registrationOrgId!),
    enabled: Boolean(registrationOrgId),
    retry: false,
    meta: { errorPresentation: 'local-initial' },
  });
  const registrationOrganization = registrationOrgQuery.data;
  const registrationMode = registrationOrgId
    ? 'ORGANIZATION'
    : detail?.value === 'UNASSIGNED'
      ? 'UNASSIGNED'
      : 'DISABLED';
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
      ...(isRegistrationPolicy
        ? ([
            {
              label: '注册策略',
              dataIndex: 'registrationMode',
              type: 'select',
              options: [
                { label: '关闭注册', value: 'DISABLED' },
                { label: '开放注册，不分配部门', value: 'UNASSIGNED' },
                { label: '开放注册，分配到指定组织', value: 'ORGANIZATION' },
              ],
              rules: [{ required: true, message: '请选择注册策略' }],
            },
            {
              label: '注册组织',
              dataIndex: 'registrationOrganization',
              type: 'ref-selector',
              refSelector: registrationOrgSelector,
              tooltip: '选择分配到指定组织时必填',
            },
            {
              label: '默认职位',
              dataIndex: 'registrationPosition',
              type: 'text',
              tooltip: '选择分配到指定组织时必填',
              rules: [{ max: 100, message: '职位不能超过100个字符' }],
            },
          ] satisfies EditField[])
        : ([
            { label: '参数值', dataIndex: 'value', type: 'textarea', fullWidth: true },
          ] satisfies EditField[])),
      {
        label: '描述',
        dataIndex: 'description',
        type: 'textarea',
        fullWidth: true,
        disabled: detail?.isSystem,
      },
    ],
    [detail?.isSystem, featureRefSelector, isRegistrationPolicy, registrationOrgSelector],
  );
  const initialValues = useMemo(
    () =>
      detail
        ? {
            number: detail.number,
            name: detail.name,
            value: detail.value ?? '',
            registrationMode,
            registrationOrganization: registrationOrgId
              ? {
                  id: registrationOrgId,
                  name: registrationOrganization?.name ?? `组织 ID ${registrationOrgId}`,
                }
              : undefined,
            registrationPosition,
            description: detail.description ?? '',
            feature: detail.feature ?? null,
          }
        : {},
    [detail, registrationMode, registrationOrgId, registrationOrganization, registrationPosition],
  );
  const saveMutation = useCommandMutation({
    mutationFn: async (values: Record<string, unknown>) => {
      const feature = values.feature as { id?: string } | null;
      let value = String(values.value ?? '');
      if (isRegistrationPolicy) {
        value = String(values.registrationMode);
        if (value === 'ORGANIZATION') {
          const organization = values.registrationOrganization as { id?: string } | undefined;
          const position = String(values.registrationPosition ?? '').trim();
          if (!organization?.id || !position) throw new Error('请选择注册组织并填写默认职位');
          value = `ORGANIZATION:${organization.id}:${position}`;
        }
      }
      const savedId = await sysParamApi.save({
        id: billId,
        version: detail?.version,
        number: String(values.number).trim(),
        name: String(values.name).trim(),
        value,
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
      loading={detailQuery.isLoading || registrationOrgQuery.isLoading}
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
