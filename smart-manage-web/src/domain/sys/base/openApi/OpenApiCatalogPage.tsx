import { useMemo, useState } from 'react';
import { Button, Descriptions, Space, Tag, Typography } from 'antd';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import AppModal from '@/domain/common/component/AppModal';
import ListTree from '@/domain/common/page/ListTree';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import type { PageComponentProps } from '@/domain/common/page/types';
import { openApiPlatformApi } from './api';
import { openApiCatalogAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import type { OpenApiRelease } from './types';
import { buildCatalogTree, catalogFilterFromTreeKey } from './catalogHierarchy';
import './OpenApiPage.css';

const OpenApiCatalogPage = (props: PageComponentProps) => {
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const [detail, setDetail] = useState<OpenApiRelease>();
  const [treeKey, setTreeKey] = useState<string>();
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  const hierarchyQuery = useQuery({
    queryKey: openApiQueryKeys.catalogList({ hierarchy: true }),
    queryFn: openApiPlatformApi.catalogHierarchy,
  });
  const catalogTree = buildCatalogTree(hierarchyQuery.data ?? []);
  const effectiveTreeKey = treeKey ?? catalogTree[0]?.key;
  const list = useListPageQuery({
    queryKey: openApiQueryKeys.catalogList({ treeKey: effectiveTreeKey }),
    queryFn: (form) =>
      openApiPlatformApi.catalogList({
        ...form,
        ...catalogFilterFromTreeKey(effectiveTreeKey),
      }),
  });
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.id === selectedKeys[0])
      : undefined;
  const statusMutation = useCommandMutation({
    mutationFn: (release: OpenApiRelease) =>
      openApiPlatformApi.catalogStatus({
        id: release.id,
        version: release.version,
        status: release.status === 'PUBLISHED' ? 'OFFLINE' : 'PUBLISHED',
      }),
    onSuccess: async () => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: openApiQueryKeys.catalog() });
      feedback.success('API 版本状态更新成功');
    },
  });
  const columns: ColumnsType<OpenApiRelease> = [
    {
      title: 'API 名称',
      dataIndex: 'name',
      width: 220,
      render: (value: string, release) => (
        <Button type="link" size="small" onClick={() => setDetail(release)}>
          {value}
        </Button>
      ),
    },
    { title: 'API 编码', dataIndex: 'apiNumber', width: 220 },
    { title: '版本', dataIndex: 'apiVersion', width: 90 },
    { title: '方法', dataIndex: 'httpMethod', width: 90 },
    { title: '路径', dataIndex: 'path', width: 360 },
    { title: '功能', dataIndex: 'featureName', width: 140 },
    { title: '所属应用', dataIndex: 'applicationName', width: 160 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: OpenApiRelease['status']) => (
        <Tag
          color={status === 'PUBLISHED' ? 'success' : status === 'DRAFT' ? 'processing' : 'default'}
        >
          {{ PUBLISHED: '已发布', DRAFT: '草稿', OFFLINE: '已下线' }[status]}
        </Tag>
      ),
    },
    {
      title: '代码注册',
      dataIndex: 'registered',
      width: 100,
      render: (registered: boolean) => (
        <Tag color={registered ? 'blue' : 'error'}>{registered ? '已注册' : '缺失'}</Tag>
      ),
    },
  ];
  const schemas = useMemo(() => {
    const format = (value?: string) => {
      if (!value) return '';
      try {
        return JSON.stringify(JSON.parse(value), null, 2);
      } catch {
        return value;
      }
    };
    return { request: format(detail?.requestSchema), response: format(detail?.responseSchema) };
  }, [detail]);
  return (
    <>
      <ListPage<OpenApiRelease>
        {...props}
        title="API 文档"
        access={openApiCatalogAccess}
        loading={list.query.isLoading}
        error={list.query.error as Error | null}
        onRetry={() => list.query.refetch()}
        total={list.total}
        pageNum={list.pageNum}
        pageSize={list.pageSize}
        quickSearchPlaceholder="搜索 API 编码/名称/操作标识"
        columnSettingsKey="sys/base/openapi-catalog/list"
        treePanel={
          <ListTreePanel>
            <ListTree
              treeData={catalogTree as unknown as DataNode[]}
              fieldNames={{ key: 'key', title: 'title', children: 'children' }}
              selectedKeys={effectiveTreeKey ? [effectiveTreeKey] : []}
              onSelect={(keys) => setTreeKey(keys[0] ? String(keys[0]) : effectiveTreeKey)}
              defaultExpandAll
              blockNode
            />
          </ListTreePanel>
        }
        toolbarActions={[
          {
            key: 'publish',
            label: selected?.status === 'PUBLISHED' ? '下线版本' : '发布版本',
            permission: openApiCatalogAccess.permissions.publish,
            disabled: !selected || !selected.registered,
            danger: selected?.status === 'PUBLISHED',
            loading: statusMutation.isPending,
            onClick: () =>
              selected &&
              void confirmOperation({
                type: selected.status === 'PUBLISHED' ? 'warning' : 'normal',
                title: `确认${selected.status === 'PUBLISHED' ? '下线' : '发布'} API 版本？`,
                description: `${selected.name} ${selected.apiVersion}`,
                confirmText: selected.status === 'PUBLISHED' ? '下线' : '发布',
                onConfirm: () => statusMutation.mutateAsync(selected),
              }),
          },
        ]}
        onRefresh={list.onRefresh}
        onQuickSearch={list.onSearch}
        onPageChange={list.onPageChange}
        rowKey="id"
        columns={columns}
        dataSource={list.records}
        selectMode="checkbox"
        selectedRowKeys={selectedKeys}
        onSelectChange={setSelectedKeys}
      />
      <AppModal
        open={Boolean(detail)}
        width={900}
        title="API 文档"
        onCancel={() => setDetail(undefined)}
        footer={<Button onClick={() => setDetail(undefined)}>关闭</Button>}
      >
        {detail && (
          <Space direction="vertical" size="large">
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
              <Descriptions.Item label="版本">{detail.apiVersion}</Descriptions.Item>
              <Descriptions.Item label="操作标识" span={2}>
                <Typography.Text code>{detail.operationKey}</Typography.Text>
              </Descriptions.Item>
              <Descriptions.Item label="请求方法">{detail.httpMethod}</Descriptions.Item>
              <Descriptions.Item label="请求路径">{detail.path}</Descriptions.Item>
              <Descriptions.Item label="所属领域">{detail.domainName}</Descriptions.Item>
              <Descriptions.Item label="所属应用">{detail.applicationName}</Descriptions.Item>
              <Descriptions.Item label="所属功能" span={2}>
                {detail.featureName}
              </Descriptions.Item>
              <Descriptions.Item label="说明" span={2}>
                {detail.description ?? '—'}
              </Descriptions.Item>
            </Descriptions>
            <Typography.Title level={5}>协议说明</Typography.Title>
            <Typography.Paragraph>{detail.documentation ?? '—'}</Typography.Paragraph>
            <Typography.Title level={5}>请求明文 JSON Schema</Typography.Title>
            <pre className="sm-openapi-schema">{schemas.request}</pre>
            <Typography.Title level={5}>响应明文 JSON Schema</Typography.Title>
            <pre className="sm-openapi-schema">{schemas.response}</pre>
          </Space>
        )}
      </AppModal>
    </>
  );
};

export default OpenApiCatalogPage;
