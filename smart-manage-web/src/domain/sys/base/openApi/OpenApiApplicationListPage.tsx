import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { openApiPlatformApi } from './api';
import { openApiApplicationAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import type { OpenApiApplication } from './types';

const EDIT_KEY = 'sys/base/openapi-application/edit';

const OpenApiApplicationListPage = (props: PageComponentProps) => {
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const feedback = useOperationFeedback();
  const list = useListPageQuery({
    queryKey: openApiQueryKeys.applicationList({}),
    queryFn: openApiPlatformApi.applicationList,
  });
  const selected =
    selectedKeys.length === 1
      ? list.records.find((record) => record.id === selectedKeys[0])
      : undefined;
  const toggle = useCommandMutation({
    mutationFn: async (record: OpenApiApplication) =>
      openApiPlatformApi.applicationEnable({
        id: record.id,
        version: record.version,
        enabled: !record.enabled,
      }),
    onSuccess: async () => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: openApiQueryKeys.applications() });
      feedback.success('状态更新成功');
    },
  });
  const columns: ColumnsType<OpenApiApplication> = [
    {
      title: '系统编码',
      dataIndex: 'number',
      width: 180,
      render: (value: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => openBillTab(props.appNumber, EDIT_KEY, record.id, OperationType.EDIT)}
        >
          {value}
        </Button>
      ),
    },
    { title: '系统名称', dataIndex: 'name', width: 200 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 90,
      render: (enabled: boolean) => (
        <Tag color={enabled ? 'success' : 'default'}>{enabled ? '启用' : '停用'}</Tag>
      ),
    },
    { title: '认证', dataIndex: 'authenticationType', width: 140, render: () => 'HMAC-SHA256' },
    {
      title: '报文加密',
      dataIndex: 'encryptionAlgorithm',
      width: 140,
      render: (value: string) =>
        ({ NONE: '无加密', AES_256_GCM: 'AES-256-GCM', SM4_GCM: 'SM4-GCM' })[value] ?? value,
    },
    {
      title: 'IP 策略',
      dataIndex: 'ipPolicyMode',
      width: 110,
      render: (value: string) =>
        ({ DISABLED: '不限制', WHITELIST: '白名单', BLACKLIST: '黑名单' })[value] ?? value,
    },
    { title: '描述', dataIndex: 'description', ellipsis: true },
  ];
  return (
    <ListPage<OpenApiApplication>
      {...props}
      title="第三方应用"
      access={openApiApplicationAccess}
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索系统编码/名称"
      columnSettingsKey="sys/base/openapi-application/list"
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY)}
      toolbarActions={[
        {
          key: 'enable',
          label: selected?.enabled ? '停用' : '启用',
          permission: openApiApplicationAccess.permissions.enable,
          disabled: !selected,
          loading: toggle.isPending,
          onClick: () =>
            selected &&
            void confirmOperation({
              type: selected.enabled ? 'warning' : 'normal',
              title: `确认${selected.enabled ? '停用' : '启用'}第三方应用？`,
              description: selected.name,
              confirmText: selected.enabled ? '停用' : '启用',
              onConfirm: () => toggle.mutateAsync(selected),
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
  );
};

export default OpenApiApplicationListPage;
