import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import EditPage from '@/domain/common/page/edit/EditPage';
import type { EditField } from '@/domain/common/page/edit/EditPage';
import { EditFormFields } from '@/domain/common/page/edit/EditFormFields';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useWorkbenchStore } from '@/stores/workbench';
import { openApiPlatformApi } from './api';
import { openApiCatalogTestAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import type { OpenApiTestResult } from './types';
import './OpenApiPage.css';

function formatRequestExample(value?: string) {
  if (!value) return '{}';
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value;
  }
}

export default function OpenApiCatalogTestPage(props: PageComponentProps) {
  const feedback = useOperationFeedback();
  const [result, setResult] = useState<OpenApiTestResult>();
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: openApiQueryKeys.catalogDetail(props.billId),
    queryFn: () => openApiPlatformApi.catalogDetail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const applicationsQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: [...openApiQueryKeys.catalogDetail(props.billId), 'test-applications'],
    queryFn: () => openApiPlatformApi.catalogTestApplications(props.billId!),
    enabled: Boolean(props.billId),
  });
  const executeMutation = useCommandMutation({
    mutationFn: (values: Record<string, unknown>) =>
      openApiPlatformApi.catalogTestExecute({
        releaseId: props.billId!,
        applicationId: String(values.applicationId),
        requestJson: String(values.requestJson),
      }),
    onSuccess: (response) => {
      setResult(response);
      feedback.success(`调用成功，耗时 ${response.durationMs} ms`);
    },
  });
  const applications = useMemo(() => applicationsQuery.data ?? [], [applicationsQuery.data]);
  const detail = detailQuery.data;
  const fields = useMemo<EditField[]>(
    () => [
      {
        label: '第三方应用',
        dataIndex: 'applicationId',
        type: 'select',
        options: applications.map((app) => ({
          label: `${app.number} - ${app.name}`,
          value: app.id,
        })),
        rules: [{ required: true, message: '请选择执行身份对应的第三方应用' }],
      },
      {
        label: '请求 JSON',
        dataIndex: 'requestJson',
        type: 'textarea',
        rows: 12,
        fullWidth: true,
        rules: [{ required: true, message: '请求 JSON 不能为空' }],
      },
      {
        label: result ? `响应数据（${result.durationMs} ms）` : '响应数据',
        dataIndex: 'response',
        type: 'custom',
        fullWidth: true,
        content: (
          <pre className="sm-openapi-schema sm-openapi-test-response">
            {result ? JSON.stringify(result.response, null, 2) : '执行后将在这里显示响应数据'}
          </pre>
        ),
      },
    ],
    [applications, result],
  );
  const initialValues = useMemo(
    () => ({
      applicationId: applications[0]?.id,
      requestJson: formatRequestExample(detail?.requestExample),
    }),
    [applications, detail],
  );
  const error = getBlockingQueryError(detailQuery) ?? getBlockingQueryError(applicationsQuery);
  return (
    <EditPage
      access={openApiCatalogTestAccess}
      title={`API 在线测试${detail ? ` - ${detail.name}` : ''}`}
      sections={[
        {
          key: 'test',
          label: '业务试调',
          content: (editable) => <EditFormFields fields={fields} editable={editable} />,
        },
      ]}
      initialValues={initialValues}
      operationType={OperationType.EDIT}
      loading={detailQuery.isLoading || applicationsQuery.isLoading}
      error={error as Error | null}
      onRetry={() => Promise.all([detailQuery.refetch(), applicationsQuery.refetch()])}
      saveLabel="执行"
      saving={executeMutation.isPending}
      onSave={(values) => executeMutation.mutateAsync(values).then(() => undefined)}
      closeGuard={{ appNumber: props.appNumber, tabKey: props.tabKey }}
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
