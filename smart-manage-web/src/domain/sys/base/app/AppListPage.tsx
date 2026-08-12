import { useState, useMemo } from 'react';
import { Tag, Button } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import ListTreePanel from '@/domain/common/page/ListTreePanel';
import ListTree from '@/domain/common/page/ListTree';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useWorkbenchStore } from '@/stores/workbench';
import { OperationType } from '@/domain/common/page/types';
import { fetchAppsAll, appApi } from './api';
import { appQueryKeys } from './queryKeys';
import type { AppListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { appAccess } from './permissions';

/** 应用编辑页 componentKey */
const APP_EDIT_KEY = 'sys/base/app/edit';

/** 应用列表页 — 左树（云）右表（应用） */
const AppListPage = (props: PageComponentProps) => {
  const [selectedCloudId, setSelectedCloudId] = useState<string | undefined>(undefined);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((s) => s.openBillTab);
  const openAddNewTab = useWorkbenchStore((s) => s.openAddNewTab);

  // 左侧树数据
  const treeQuery = useQuery({
    queryKey: appQueryKeys.cloudAppsAll(),
    queryFn: fetchAppsAll,
    staleTime: 5 * 60 * 1000,
  });

  const treeData: DataNode[] = useMemo(
    () => [
      {
        key: 'root',
        title: '全部云',
        children:
          treeQuery.data?.map((cloud) => ({
            key: cloud.id,
            title: cloud.name,
            isLeaf: true,
          })) ?? [],
      },
    ],
    [treeQuery.data],
  );

  // 右侧列表
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: appQueryKeys.list({ cloudId: selectedCloudId }),
      queryFn: (params) =>
        appApi.listPage({
          ...params,
          cloudId: selectedCloudId,
        }),
    });
  const enabledMutation = useEnabledMutation(appApi.setEnabled, async () => {
    setSelectedRowKeys([]);
    await Promise.all([query.refetch(), treeQuery.refetch()]);
  });

  const handleTreeSelect = (keys: React.Key[]) => {
    if (keys.length === 0 || keys[0] === 'root') {
      setSelectedCloudId(undefined);
    } else {
      setSelectedCloudId(String(keys[0]));
    }
  };

  const handleOpenEdit = (id: string) => {
    openBillTab(props.appNumber, APP_EDIT_KEY, id, OperationType.EDIT);
  };

  const handleOpenAdd = () => {
    openAddNewTab(props.appNumber, APP_EDIT_KEY);
  };

  const columns: ColumnsType<AppListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 150,
      render: (text, record) => (
        <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 200 },
    { title: '所属云', dataIndex: 'cloudName', width: 120 },
    { title: '排序', dataIndex: 'seq', width: 80 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 80,
      render: (value) => (value ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>),
    },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];

  const treePanel = (
    <ListTreePanel>
      <ListTree
        virtual={false}
        treeData={treeData}
        showLine={false}
        blockNode
        defaultExpandedKeys={['root']}
        defaultSelectedKeys={['root']}
        onSelect={handleTreeSelect}
      />
    </ListTreePanel>
  );

  return (
    <ListPage<AppListVO>
      {...props}
      title="应用"
      access={appAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      treePanel={treePanel}
      onAddNew={handleOpenAdd}
      onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
      onDisable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })}
      enabledCommandLoading={enabledMutation.isPending}
      onRefresh={onRefresh}
      onQuickSearch={onSearch}
      onPageChange={onPageChange}
      rowKey="id"
      columns={columns}
      dataSource={records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={(keys) => setSelectedRowKeys(keys)}
    />
  );
};

export default AppListPage;
