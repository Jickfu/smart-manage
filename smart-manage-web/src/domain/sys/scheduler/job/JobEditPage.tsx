import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { isErrorFeedbackSuppressed } from '@/api/errorPresentation';
import { RequestErrorDescription } from '@/domain/common/component/RequestErrorState';
import { Button } from 'antd';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { createBillTabKey } from '@/domain/common/page/tab/tabKeys';
import EditPage from '@/domain/common/page/edit/EditPage';
import { editFormSection } from '@/domain/common/page/edit/editPageSection';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { jobApi } from './api';
import { jobAccess } from './permissions';
import { jobQueryKeys } from './queryKeys';
import type { JobClassOption } from './types';

const cronPresets = [
  { label: '每天 08:00', value: '0 0 8 * * ?' },
  { label: '每 5 分钟', value: '0 0/5 * * * ?' },
  { label: '每 10 分钟', value: '0 0/10 * * * ?' },
  { label: '每 30 分钟', value: '0 0/30 * * * ?' },
  { label: '每小时整点', value: '0 0 * * * ?' },
  { label: '每周一 08:00', value: '0 0 8 ? * MON' },
  { label: '每月 1 日 08:00', value: '0 0 8 1 * ?' },
];

const JobEditPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const isAddNew = props.operationType === OperationType.ADDNEW;
  const [cronExpression, setCronExpression] = useState('');
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: jobQueryKeys.detail(props.billId),
    queryFn: () => jobApi.detail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const defaultQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: jobQueryKeys.createNewData(),
    queryFn: jobApi.createNewData,
    enabled: isAddNew,
  });
  const classesQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: jobQueryKeys.classes(),
    queryFn: jobApi.classes,
    staleTime: 5 * 60 * 1000,
  });
  const detail = detailQuery.data;
  const effectiveCron =
    cronExpression || detail?.cronExpression || defaultQuery.data?.cronExpression || '';
  const cronPreviewQuery = useQuery({
    meta: { errorPresentation: 'local' },
    queryKey: jobQueryKeys.cronPreview(effectiveCron),
    queryFn: () => jobApi.cronPreview(effectiveCron),
    enabled: Boolean(effectiveCron),
    retry: false,
  });
  const refreshCronPreview = cronPreviewQuery.refetch;
  const jobClassSelector = useMemo(
    () =>
      defineRefSelector<JobClassOption>({
        selectorKey: 'scheduler-job-class',
        modalTitle: '选择执行类',
        fetchFn: async ({ pageNum, pageSize, keyword }) => {
          const normalizedKeyword = keyword?.trim().toLowerCase();
          const records = (classesQuery.data ?? []).filter(
            (item) =>
              !normalizedKeyword ||
              item.simpleName.toLowerCase().includes(normalizedKeyword) ||
              item.className.toLowerCase().includes(normalizedKeyword) ||
              item.description.toLowerCase().includes(normalizedKeyword),
          );
          const start = (pageNum - 1) * pageSize;
          return { records: records.slice(start, start + pageSize), total: records.length };
        },
        displayRender: (record) => `${record.simpleName}（${record.className}）`,
        fieldNames: { key: 'className', label: 'simpleName' },
        columns: [
          { title: '执行类', dataIndex: 'simpleName', width: 180 },
          { title: '完整类名', dataIndex: 'className', width: 360 },
          { title: '用途说明', dataIndex: 'description' },
        ],
      }),
    [classesQuery.data],
  );
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '任务编码',
        dataIndex: 'number',
        type: 'text',
        disabled: detail?.isSystem,
        rules: [{ required: true, message: '任务编码不能为空' }],
      },
      {
        label: '任务名称',
        dataIndex: 'jobName',
        type: 'text',
        disabled: detail?.isSystem,
        rules: [{ required: true, message: '任务名称不能为空' }],
      },
      {
        label: '任务分组',
        dataIndex: 'jobGroup',
        type: 'text',
        disabled: detail?.isSystem,
        rules: [{ required: true, message: '任务分组不能为空' }],
      },
      {
        label: '状态',
        dataIndex: 'status',
        type: 'select',
        disabled: true,
        options: [
          { label: '已启用', value: 'ENABLED' },
          { label: '已暂停', value: 'PAUSED' },
        ],
      },
      {
        label: '执行类',
        dataIndex: 'jobClass',
        type: 'ref-selector',
        columnSpan: 2,
        disabled: detail?.isSystem,
        refSelector: jobClassSelector,
        rules: [{ required: true, message: '执行类不能为空' }],
      },
      {
        label: '共享资源互斥键',
        dataIndex: 'mutexKey',
        type: 'text',
        columnSpan: 2,
        disabled: detail?.isSystem,
        placeholder: '相同互斥键的任务不会同时执行；留空表示不互斥',
      },
      {
        label: '常用执行频率',
        dataIndex: 'cronPreset',
        type: 'select',
        columnSpan: 2,
        placeholder: '选择常用频率或直接填写 Cron',
        options: cronPresets,
      },
      {
        label: 'Cron 表达式',
        dataIndex: 'cronExpression',
        type: 'text',
        columnSpan: 2,
        rules: [{ required: true, message: 'Cron 表达式不能为空' }],
      },
      {
        label: '未来五次执行时间（服务端时区）',
        dataIndex: 'cronPreview',
        type: 'custom',
        content: cronPreviewQuery.isFetching ? (
          '正在计算…'
        ) : cronPreviewQuery.error && !isErrorFeedbackSuppressed(cronPreviewQuery.error) ? (
          <>
            <RequestErrorDescription
              error={cronPreviewQuery.error}
              fallbackMessage="执行时间计算失败"
            />
            <Button type="link" onClick={() => void refreshCronPreview()}>
              重试
            </Button>
          </>
        ) : (
          (cronPreviewQuery.data?.join('；') ?? '暂无可执行时间')
        ),
        fullWidth: true,
      },
      { label: '任务参数（JSON）', dataIndex: 'jobData', type: 'textarea', fullWidth: true },
      { label: '描述', dataIndex: 'description', type: 'textarea', fullWidth: true },
    ],
    [
      cronPreviewQuery.data,
      cronPreviewQuery.error,
      refreshCronPreview,
      cronPreviewQuery.isFetching,
      detail?.isSystem,
      jobClassSelector,
    ],
  );
  const initialValues = useMemo(
    () => ({
      ...(detail ?? {}),
      jobGroup: detail?.jobGroup ?? defaultQuery.data?.jobGroup ?? 'DEFAULT',
      status: detail?.status ?? defaultQuery.data?.status ?? 'PAUSED',
      cronExpression: detail?.cronExpression ?? defaultQuery.data?.cronExpression ?? '0 0 3 * * ?',
      jobData: detail?.jobData ?? '',
      mutexKey: detail?.mutexKey ?? '',
      jobClass: classesQuery.data?.find((item) => item.className === detail?.jobClassName) ?? null,
    }),
    [classesQuery.data, defaultQuery.data, detail],
  );
  const saveMutation = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) => {
      const jobClass = values.jobClass as JobClassOption;
      return jobApi.save({
        id: props.billId,
        version: detail?.version,
        number: String(values.number).trim(),
        jobName: String(values.jobName).trim(),
        jobGroup: String(values.jobGroup).trim(),
        jobClassName: jobClass.className,
        cronExpression: String(values.cronExpression).trim(),
        jobData: String(values.jobData ?? ''),
        mutexKey: String(values.mutexKey ?? ''),
        description: String(values.description ?? ''),
      });
    },
    onSuccess: async (savedId) => {
      if (isAddNew) {
        const nextKey = createBillTabKey(props.componentKey, savedId);
        useWorkbenchStore.getState().replaceContentTab(props.appNumber, props.tabKey, {
          key: nextKey,
          closable: true,
          componentKey: props.componentKey,
          pageType: 'EDIT',
          operationType: OperationType.EDIT,
          billId: savedId,
        });
        useWorkbenchStore.getState().activateContentTab(props.appNumber, nextKey);
      }
      await queryClient.invalidateQueries({ queryKey: jobQueryKeys.all });
      feedback.success(isAddNew ? '新增成功' : '保存成功');
    },
  });

  return (
    <EditPage
      title="定时任务"
      access={jobAccess}
      sections={[editFormSection('basic', '基本信息', fields)]}
      initialValues={initialValues}
      operationType={props.operationType ?? OperationType.EDIT}
      closeGuard={{ appNumber: props.appNumber, tabKey: props.tabKey }}
      loading={detailQuery.isLoading || defaultQuery.isLoading || classesQuery.isLoading}
      error={
        (getBlockingQueryError(detailQuery) ??
          getBlockingQueryError(defaultQuery) ??
          getBlockingQueryError(classesQuery)) as Error | null
      }
      onRetry={() =>
        Promise.all([detailQuery.refetch(), defaultQuery.refetch(), classesQuery.refetch()])
      }
      onValuesChange={(changedValues, _allValues, form) => {
        if ('jobClass' in changedValues) {
          const selectedClass = changedValues.jobClass as JobClassOption | null;
          if (selectedClass) {
            form.setFieldsValue({
              description: selectedClass.description,
              jobData: selectedClass.parameterTemplate,
            });
          }
        }
        if ('cronPreset' in changedValues && changedValues.cronPreset) {
          form.setFieldValue('cronExpression', changedValues.cronPreset);
          setCronExpression(String(changedValues.cronPreset));
        }
        if ('cronExpression' in changedValues) {
          setCronExpression(String(changedValues.cronExpression ?? ''));
        }
      }}
      onSave={async (values) => {
        await saveMutation.mutateAsync(values);
      }}
      saving={saveMutation.isPending}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
};

export default JobEditPage;
