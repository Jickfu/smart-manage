import { useMemo, useState } from 'react';
import { App, Button, Tag, Tree } from 'antd';
import type { DataNode } from 'antd/es/tree';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useWorkbenchStore } from '@/stores/workbench';
import { fetchAppsAll } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { useQuery } from '@tanstack/react-query';
import { sysParamApi } from './api';
import { sysParamAccess } from './permissions';
import { sysParamQueryKeys } from './queryKeys';
import type { SysParamVO } from './types';

const EDIT_KEY = componentKeys.sysParamEdit;

type Scope =
  | { type: 'all' }
  | { type: 'global' }
  | { type: 'cloud'; id: string }
  | { type: 'app'; id: string };

const SysParamListPage = (props: PageComponentProps) => {
  const { modal, message } = App.useApp();
  const [scope, setScope] = useState<Scope>({ type: 'all' });
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const treeQuery = useQuery({
    queryKey: appQueryKeys.cloudAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });
  const scopeParams = {
    appId: scope.type === 'app' ? scope.id : undefined,
    cloudId: scope.type === 'cloud' ? scope.id : undefined,
    globalOnly: scope.type === 'global' ? true : undefined,
  };
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: sysParamQueryKeys.list(scopeParams),
      queryFn: (params) => sysParamApi.listPage({ ...params, ...scopeParams }),
    });
  const deleteMutation = useCommandMutation({
    mutationFn: sysParamApi.delete,
    onSuccess: async () => {
      setSelectedRowKeys([]);
      await queryClient.invalidateQueries({ queryKey: sysParamQueryKeys.all });
      message.success('删除成功');
    },
  });
  const treeData = useMemo<DataNode[]>(
    () => [
      {
        key: 'all',
        title: '全部参数',
        children: [
          { key: 'global', title: '全局参数', isLeaf: true },
          ...(treeQuery.data?.map((cloud) => ({
            key: `cloud:${cloud.id}`,
            title: cloud.name,
            children: cloud.appList.map((application) => ({
              key: `app:${application.id}`,
              title: application.name,
              isLeaf: true,
            })),
          })) ?? []),
        ],
      },
    ],
    [treeQuery.data],
  );
  const columns: ColumnsType<SysParamVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (text, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(props.appNumber, EDIT_KEY, '编辑系统参数', record.id, OperationType.EDIT)
          }
        >
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: '所属应用', dataIndex: 'appName', width: 160, render: (value) => value ?? '全局参数' },
    { title: '参数值', dataIndex: 'value', ellipsis: true },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    {
      title: '类型',
      dataIndex: 'isSystem',
      width: 100,
      render: (value) => (value ? <Tag color="blue">系统内置</Tag> : <Tag>自定义</Tag>),
    },
  ];
  const selectedRecords = records.filter((record) => selectedRowKeys.includes(record.id));
  return (
    <ListPage<SysParamVO>
      {...props}
      title="系统参数"
      access={sysParamAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      treePanel={
        <div className="sm-list-tree-panel-inner">
          <Tree
            treeData={treeData}
            blockNode
            defaultExpandedKeys={['all']}
            defaultSelectedKeys={['all']}
            onSelect={(keys) => {
              const key = String(keys[0] ?? 'all');
              if (key === 'global') setScope({ type: 'global' });
              else if (key.startsWith('cloud:')) setScope({ type: 'cloud', id: key.slice(6) });
              else if (key.startsWith('app:')) setScope({ type: 'app', id: key.slice(4) });
              else setScope({ type: 'all' });
            }}
          />
        </div>
      }
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY, '新增系统参数')}
      toolbarActions={[
        {
          key: 'delete',
          label: '删除',
          permission: sysParamAccess.permissions.delete,
          danger: true,
          disabled: selectedRowKeys.length !== 1 || selectedRecords[0]?.isSystem,
          loading: deleteMutation.isPending,
          onClick: () => {
            const record = selectedRecords[0];
            if (!record) return;
            modal.confirm({
              title: '确认删除系统参数？',
              content: record.number,
              okButtonProps: { danger: true },
              onOk: () => deleteMutation.mutateAsync(record.id),
            });
          },
        },
      ]}
      onRefresh={onRefresh}
      onQuickSearch={onSearch}
      onPageChange={onPageChange}
      rowKey="id"
      columns={columns}
      dataSource={records}
      selectMode="radio"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
    />
  );
};

export default SysParamListPage;
