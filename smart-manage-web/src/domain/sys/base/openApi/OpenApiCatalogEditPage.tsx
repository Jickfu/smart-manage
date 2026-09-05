import { useMemo, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Anchor, Descriptions, Table, Tag, Typography } from 'antd';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import EditPage from '@/domain/common/page/edit/EditPage';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { useUserStore } from '@/stores/user';
import { openApiPlatformApi } from './api';
import { openApiCatalogAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import type { OpenApiRelease } from './types';
import JsonCodeViewer from './JsonCodeViewer';
import './OpenApiPage.css';

const statusNames: Record<OpenApiRelease['status'], string> = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  OFFLINE: '已下线',
};

interface JsonSchema {
  type?: string | string[];
  description?: string;
  required?: string[];
  properties?: Record<string, JsonSchema>;
  items?: JsonSchema;
  example?: unknown;
  default?: unknown;
}

interface SchemaRow {
  key: string;
  name: string;
  type: string;
  required: boolean;
  description: string;
  level: number;
  example: string;
}

function parseJson(value?: string): unknown {
  if (!value) return undefined;
  try {
    return JSON.parse(value);
  } catch {
    return undefined;
  }
}

function formatJson(value?: string) {
  const parsed = parseJson(value);
  return parsed === undefined ? value || '—' : JSON.stringify(parsed, null, 2);
}

function typeName(schema: JsonSchema): string {
  const type = Array.isArray(schema.type) ? schema.type.join(' | ') : schema.type || 'object';
  if (type === 'array' && schema.items) return `array<${typeName(schema.items)}>`;
  return type;
}

function displayExample(value: unknown) {
  if (value === undefined) return '—';
  return typeof value === 'string' ? value : JSON.stringify(value);
}

function buildSchemaRows(schemaText?: string, exampleText?: string): SchemaRow[] {
  const schema = parseJson(schemaText) as JsonSchema | undefined;
  const example = parseJson(exampleText);
  if (!schema?.properties) return [];
  const rows: SchemaRow[] = [];
  const appendProperties = (
    properties: Record<string, JsonSchema>,
    requiredNames: string[],
    parentKey: string,
    level: number,
    exampleValue: unknown,
  ) => {
    Object.entries(properties).forEach(([name, property]) => {
      const key = parentKey ? `${parentKey}.${name}` : name;
      const propertyExample =
        exampleValue && typeof exampleValue === 'object' && !Array.isArray(exampleValue)
          ? (exampleValue as Record<string, unknown>)[name]
          : undefined;
      rows.push({
        key,
        name,
        type: typeName(property),
        required: requiredNames.includes(name),
        description: property.description || '—',
        level,
        example: displayExample(propertyExample ?? property.example ?? property.default),
      });
      const nestedSchema = property.type === 'array' ? property.items : property;
      const nestedExample = Array.isArray(propertyExample) ? propertyExample[0] : propertyExample;
      if (nestedSchema?.properties) {
        appendProperties(
          nestedSchema.properties,
          nestedSchema.required ?? [],
          key,
          level + 1,
          nestedExample,
        );
      }
    });
  };
  appendProperties(schema.properties, schema.required ?? [], '', 1, example);
  return rows;
}

const schemaColumns = [
  { title: '参数名称', dataIndex: 'name', key: 'name' },
  { title: '参数类型', dataIndex: 'type', key: 'type', width: 150 },
  {
    title: '必填',
    dataIndex: 'required',
    key: 'required',
    width: 72,
    render: (required: boolean) => (required ? '是' : '否'),
  },
  { title: '说明', dataIndex: 'description', key: 'description' },
  { title: '层级', dataIndex: 'level', key: 'level', width: 72 },
  { title: '示例', dataIndex: 'example', key: 'example' },
];

function ApiDocument({ detail }: { detail: OpenApiRelease }) {
  const documentRef = useRef<HTMLElement>(null);
  const sectionPrefix = `openapi-${detail.id}`;
  const requestRows = useMemo(
    () => buildSchemaRows(detail.requestSchema, detail.requestExample),
    [detail.requestExample, detail.requestSchema],
  );
  const responseRows = useMemo(
    () => buildSchemaRows(detail.responseSchema, detail.responseExample),
    [detail.responseExample, detail.responseSchema],
  );
  const sections = {
    basic: `${sectionPrefix}-basic`,
    request: `${sectionPrefix}-request`,
    response: `${sectionPrefix}-response`,
    examples: `${sectionPrefix}-examples`,
  };
  return (
    <article ref={documentRef} className="sm-openapi-document">
      <main className="sm-openapi-document__content">
        <Typography.Title level={2}>{detail.name}</Typography.Title>
        <section id={sections.basic} className="sm-openapi-document__section">
          <Typography.Title level={4}>接口基本信息</Typography.Title>
          <Descriptions
            className="sm-openapi-document__basic"
            bordered
            size="small"
            column={{ xs: 1, sm: 1, md: 2, lg: 2, xl: 2, xxl: 2 }}
            items={[
              { key: 'apiNumber', label: 'API 编码', children: detail.apiNumber },
              {
                key: 'apiVersion',
                label: '适用版本',
                children: <Tag color="blue">{detail.apiVersion}</Tag>,
              },
              {
                key: 'httpMethod',
                label: '请求方式',
                children: <Tag color="geekblue">{detail.httpMethod}</Tag>,
              },
              {
                key: 'status',
                label: '发布状态',
                children: (
                  <Tag color={detail.status === 'PUBLISHED' ? 'success' : 'default'}>
                    {statusNames[detail.status]}
                  </Tag>
                ),
              },
              {
                key: 'registered',
                label: '代码注册',
                children: (
                  <Tag color={detail.registered ? 'processing' : 'error'}>
                    {detail.registered ? '已注册' : '代码缺失'}
                  </Tag>
                ),
              },
              {
                key: 'module',
                label: '所属模块',
                children: `${detail.domainName} / ${detail.applicationName} / ${detail.featureName}`,
              },
              {
                key: 'path',
                label: '请求 URL',
                span: 'filled',
                children: (
                  <Typography.Text copyable={{ text: detail.path }}>{detail.path}</Typography.Text>
                ),
              },
              {
                key: 'operationKey',
                label: '操作标识',
                span: 'filled',
                children: detail.operationKey,
              },
              {
                key: 'description',
                label: '用途说明',
                span: 'filled',
                children: detail.description || '—',
              },
            ]}
          />
        </section>

        <section id={sections.request} className="sm-openapi-document__section">
          <Typography.Title level={4}>请求体参数</Typography.Title>
          <Table<SchemaRow>
            bordered
            size="small"
            pagination={false}
            rowKey="key"
            columns={schemaColumns}
            dataSource={requestRows}
            locale={{ emptyText: '该接口没有请求体参数' }}
            scroll={{ x: 760 }}
          />
        </section>

        <section id={sections.response} className="sm-openapi-document__section">
          <Typography.Title level={4}>返回参数</Typography.Title>
          <Table<SchemaRow>
            bordered
            size="small"
            pagination={false}
            rowKey="key"
            columns={schemaColumns.filter((column) => column.key !== 'required')}
            dataSource={responseRows}
            locale={{ emptyText: '该接口没有返回参数定义' }}
            scroll={{ x: 680 }}
          />
        </section>

        <section id={sections.examples} className="sm-openapi-document__section">
          <Typography.Title level={4}>请求结构示例</Typography.Title>
          <JsonCodeViewer value={formatJson(detail.requestExample)} />
          <Typography.Title level={4} className="sm-openapi-document__response-title">
            返回结构示例
          </Typography.Title>
          <JsonCodeViewer value={formatJson(detail.responseExample)} />
        </section>
      </main>
      <aside className="sm-openapi-document__anchor">
        <Anchor
          affix={false}
          getContainer={() =>
            documentRef.current?.closest<HTMLElement>('.sm-edit-body') ?? document.documentElement
          }
          targetOffset={12}
          items={[
            { key: 'basic', href: `#${sections.basic}`, title: '基本信息' },
            { key: 'request', href: `#${sections.request}`, title: '请求体参数' },
            { key: 'response', href: `#${sections.response}`, title: '返回参数' },
            { key: 'examples', href: `#${sections.examples}`, title: '结构示例' },
          ]}
        />
      </aside>
    </article>
  );
}

export default function OpenApiCatalogEditPage(props: PageComponentProps) {
  const username = useUserStore((state) => state.userInfo?.username);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const detailQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: openApiQueryKeys.catalogDetail(props.billId),
    queryFn: () => openApiPlatformApi.catalogDetail(props.billId!),
    enabled: Boolean(props.billId),
  });
  const detail = detailQuery.data;
  return (
    <EditPage
      access={openApiCatalogAccess}
      title="API 文档"
      sections={[
        {
          key: 'document',
          label: 'API 文档',
          content: () => (detail ? <ApiDocument detail={detail} /> : null),
        },
      ]}
      operationType={OperationType.VIEW}
      loading={detailQuery.isLoading}
      error={getBlockingQueryError(detailQuery) as Error | null}
      onRetry={() => detailQuery.refetch()}
      headerActions={
        username === 'administrator' && detail?.testable
          ? [
              {
                key: 'test',
                label: '测试调用',
                permission: openApiCatalogAccess.permissions.test,
                type: 'primary',
                disabled: detail.status !== 'PUBLISHED',
                onClick: () =>
                  openBillTab(
                    props.appNumber,
                    componentKeys.openApiCatalogTest,
                    detail.id,
                    OperationType.EDIT,
                  ),
              },
            ]
          : undefined
      }
      onExit={() => useWorkbenchStore.getState().removeContentTab(props.appNumber, props.tabKey)}
    />
  );
}
