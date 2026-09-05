import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Form } from 'antd';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import JsonCodeEditor from '@/domain/common/component/JsonCodeEditor';
import RefSelector from '@/domain/common/component/RefSelector';
import EditPage from '@/domain/common/page/edit/EditPage';
import { FormFieldCell, FormFieldGrid } from '@/domain/common/page/edit/FormFieldLayout';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useWorkbenchStore } from '@/stores/workbench';
import { openApiPlatformApi } from './api';
import { openApiCatalogTestAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import { useOpenApiTestApplicationRefSelector } from './refSelector/useOpenApiTestApplicationRefSelector';
import type { OpenApiTestApplication, OpenApiTestResult } from './types';
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
    mutationFn: (values: Record<string, unknown>) => {
      const application = values.application as OpenApiTestApplication;
      return openApiPlatformApi.catalogTestExecute({
        releaseId: props.billId!,
        applicationId: application.id,
        requestJson: String(values.requestJson),
      });
    },
    onSuccess: (response) => {
      setResult(response);
      feedback.success(`调用成功，耗时 ${response.durationMs} ms`);
    },
  });
  const applications = useMemo(() => applicationsQuery.data ?? [], [applicationsQuery.data]);
  const detail = detailQuery.data;
  const applicationRefSelector = useOpenApiTestApplicationRefSelector(props.billId, applications);
  const initialValues = useMemo(
    () => ({
      application: applications[0],
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
          content: (editable) => (
            <FormFieldGrid>
              <FormFieldCell>
                <Form.Item
                  name="application"
                  label="第三方应用"
                  rules={[{ required: true, message: '请选择执行身份对应的第三方应用' }]}
                  className="sm-edit-field-content"
                >
                  <RefSelector disabled={!editable} {...applicationRefSelector} />
                </Form.Item>
              </FormFieldCell>
              <FormFieldCell fullWidth>
                <Form.Item
                  name="requestJson"
                  label="请求 JSON"
                  rules={[{ required: true, message: '请求 JSON 不能为空' }]}
                  className="sm-edit-field-content"
                >
                  <JsonCodeEditor ariaLabel="请求 JSON" readOnly={!editable} />
                </Form.Item>
              </FormFieldCell>
              <FormFieldCell fullWidth>
                <Form.Item
                  label={result ? `响应数据（${result.durationMs} ms）` : '响应数据'}
                  className="sm-edit-field-content"
                >
                  <JsonCodeEditor
                    value={result ? JSON.stringify(result.response, null, 2) : '{}'}
                    ariaLabel="响应数据"
                    readOnly
                  />
                </Form.Item>
              </FormFieldCell>
            </FormFieldGrid>
          ),
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
