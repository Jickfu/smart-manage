import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { scriptApi } from './api';
import { scriptAccess } from './permissions';
import { scriptQueryKeys } from './queryKeys';
import type { ScriptListItem } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';

const EDIT_KEY = componentKeys.scriptManageEdit;

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  name: { label: '名称', filter: { type: 'string' }, sorter: true },
  description: { label: '描述', filter: { type: 'string' } },
  updateTime: { label: '更新时间', filter: { type: 'date' }, sorter: true },
  createTime: { label: '创建时间', filter: { type: 'date' }, sorter: true },
};

export default function ScriptListPage(props: PageComponentProps) {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const queryClient = useQueryClient();
  const [selectedKeys, setSelectedKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const {
    records,
    total,
    pageNum,
    pageSize,
    keyword,
    query,
    onSearch,
    onPageChange,
    onRefresh,
    columnQueryProps,
  } = useListPageQuery({
    queryKey: scriptQueryKeys.lists(),
    queryFn: scriptApi.listPage,
  });
  const deleteMutation = useCommandMutation({
    mutationFn: async () => {
      const selected = records.filter((record) => selectedKeys.includes(record.id));
      for (const script of selected) await scriptApi.delete(script.id, script.version);
    },
    onSuccess: async () => {
      setSelectedKeys([]);
      await queryClient.invalidateQueries({ queryKey: scriptQueryKeys.all });
    },
    successMessage: '删除成功',
  });
  const columns: ColumnsType<ScriptListItem> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (value, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => openBillTab(props.appNumber, EDIT_KEY, record.id, OperationType.EDIT)}
        >
          {value}
        </Button>
      ),
    },
    { title: '名称', dataIndex: 'name', width: 220 },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', width: 180 },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  return (
    <ListPage<ScriptListItem>
      {...props}
      title="脚本"
      access={scriptAccess}
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={total}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索编码/名称"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onQuickSearch={onSearch}
      onRefresh={onRefresh}
      onPageChange={onPageChange}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY)}
      onDelete={() => {
        if (selectedKeys.length === 0) {
          feedback.warning('请选择要删除的脚本');
          return;
        }
        void confirmOperation({
          type: 'delete',
          title: '确认删除选中的脚本？',
          description: '执行历史中的脚本快照不会被删除。',
          confirmText: '确认删除',
          onConfirm: () => deleteMutation.mutateAsync(),
        });
      }}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...columnQueryProps}
      dataSource={records}
      selectMode="checkbox"
      selectedRowKeys={selectedKeys}
      onSelectChange={setSelectedKeys}
    />
  );
}
