import { useState } from 'react';
import { App, Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { useEnabledMutation } from '@/domain/common/page/useEnabledMutation';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { basicDataApi } from './api';
import { basicDataAccess } from './permissions';
import { basicDataQueryKeys } from './queryKeys';
import type { BasicDataListVO } from './types';

const EDIT_KEY = 'sys/base/basic-data/edit';

const BasicDataListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const queryClient = useQueryClient();
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: basicDataQueryKeys.list({}),
      queryFn: basicDataApi.listPage,
    });
  const refreshList = async () => {
    setSelectedRowKeys([]);
    await queryClient.invalidateQueries({ queryKey: basicDataQueryKeys.all });
  };
  const enabledMutation = useEnabledMutation(basicDataApi.setEnabled, refreshList);
  const deleteMutation = useCommandMutation({
    mutationFn: ({ id, version }: { id: string; version: number }) =>
      basicDataApi.delete(id, version),
    successMessage: '删除成功',
    onSuccess: refreshList,
  });
  const selectedRecords = records.filter((record) => selectedRowKeys.includes(record.id));
  const columns: ColumnsType<BasicDataListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (text, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(props.appNumber, EDIT_KEY, '编辑基础数据', record.id, OperationType.EDIT)
          }
        >
          {text}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 200 },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 90,
      render: (enabled) =>
        enabled ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>,
    },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  ];

  return (
    <ListPage<BasicDataListVO>
      {...props}
      title="基础数据管理"
      access={basicDataAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY, '新增基础数据')}
      onEnable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: true })}
      onDisable={() => enabledMutation.mutate({ ids: selectedRowKeys.map(String), enabled: false })}
      enabledCommandLoading={enabledMutation.isPending}
      toolbarActions={[
        {
          key: 'delete',
          label: '删除',
          permission: basicDataAccess.permissions.delete,
          danger: true,
          disabled: selectedRecords.length !== 1,
          loading: deleteMutation.isPending,
          onClick: () => {
            const record = selectedRecords[0];
            if (!record) return;
            modal.confirm({
              title: '确认删除基础数据？',
              content: `${record.number} - ${record.name}`,
              okButtonProps: { danger: true },
              onOk: () => deleteMutation.mutateAsync({ id: record.id, version: record.version }),
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
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
    />
  );
};

export default BasicDataListPage;
