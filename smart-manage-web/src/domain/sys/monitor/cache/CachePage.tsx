import { App, Alert, Button, Card, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { PermissionActions } from '@/domain/common/page/PermissionActions';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import type { PageComponentProps } from '@/domain/common/page/types';
import { cacheApi } from './api';
import { cacheAccess } from './permissions';
import { cacheQueryKeys } from './queryKeys';
import type { ManagedCache } from './types';
import './cachePage.css';

export default function CachePage(_: PageComponentProps) {
  const { modal } = App.useApp();
  const queryClient = useQueryClient();
  const query = useQuery({ queryKey: cacheQueryKeys.overview(), queryFn: cacheApi.overview });
  const refresh = () => queryClient.invalidateQueries({ queryKey: cacheQueryKeys.all });
  const clearMutation = useCommandMutation({
    mutationFn: cacheApi.clear,
    successMessage: '应用缓存已清理',
    onSuccess: refresh,
  });
  const clearAllMutation = useCommandMutation({
    mutationFn: cacheApi.clearAll,
    successMessage: '全部应用缓存已清理',
    onSuccess: refresh,
  });
  const confirmClear = (cache: ManagedCache) =>
    modal.confirm({
      title: `清理“${cache.displayName}”缓存？`,
      content: cache.currentNodeOnly ? '该操作只影响当前应用节点。' : '该操作会清理共享远程缓存。',
      okText: '确认清理',
      okButtonProps: { danger: true },
      onOk: () => clearMutation.mutateAsync(cache.name),
    });
  const columns: ColumnsType<ManagedCache> = [
    {
      title: '缓存',
      dataIndex: 'displayName',
      render: (value, record) => (
        <>
          <strong>{value}</strong>
          <div className="sm-cache-code">{record.name}</div>
        </>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 110,
      render: (value) => <Tag color={value === 'LOCAL' ? 'blue' : 'purple'}>{value}</Tag>,
    },
    { title: '用途', dataIndex: 'description' },
    {
      title: '过期时间',
      dataIndex: 'expireSeconds',
      width: 120,
      render: (value) => `${value / 60} 分钟`,
    },
    { title: '条目数', dataIndex: 'estimatedSize', width: 100, render: (value) => value ?? '-' },
    {
      title: '范围',
      dataIndex: 'currentNodeOnly',
      width: 120,
      render: (value) => (value ? '当前节点' : '共享 Redis'),
    },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <PermissionActions
          prefix={cacheAccess.prefix}
          actions={[
            {
              key: 'clear',
              label: '清理',
              permission: cacheAccess.permissions.clear,
              danger: true,
              loading: clearMutation.isPending && clearMutation.variables === record.name,
              onClick: () => confirmClear(record),
            },
          ]}
        />
      ),
    },
  ];
  return (
    <div className="sm-cache-page">
      {query.error && <Alert type="error" showIcon title={query.error.message} />}
      <Card
        title="应用缓存"
        extra={
          <>
            <Button onClick={() => query.refetch()}>刷新</Button>
            <PermissionActions
              prefix={cacheAccess.prefix}
              actions={[
                {
                  key: 'clearAll',
                  label: '清理全部',
                  permission: cacheAccess.permissions.clearAll,
                  danger: true,
                  loading: clearAllMutation.isPending,
                  onClick: () =>
                    modal.confirm({
                      title: '清理全部应用缓存？',
                      content: '只会清理受控应用缓存，不会执行 Redis FLUSHDB。',
                      okText: '确认清理全部',
                      okButtonProps: { danger: true },
                      onOk: () => clearAllMutation.mutateAsync(),
                    }),
                },
              ]}
            />
          </>
        }
      >
        <Alert type="info" showIcon title="LOCAL 缓存的统计和清理仅作用于当前应用节点。" />
        <Table
          rowKey="name"
          loading={query.isLoading}
          columns={columns}
          dataSource={query.data?.caches ?? []}
          pagination={false}
        />
      </Card>
    </div>
  );
}
