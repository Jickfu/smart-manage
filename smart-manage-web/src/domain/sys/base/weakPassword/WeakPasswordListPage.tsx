import { useState } from 'react';
import { Button } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { useListSelection } from '@/domain/common/page/list/useListSelection';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { weakPasswordApi } from './api';
import { weakPasswordAccess } from './permissions';
import { weakPasswordQueryKeys } from './queryKeys';
import type { WeakPasswordVO } from './types';
import WeakPasswordEditPage from './WeakPasswordEditPage';

const columnFeatures: ListColumnFeatures = {
  word: { label: '弱口令', filter: { type: 'string' }, sorter: true },
  description: { label: '描述', filter: { type: 'string' } },
};

export default function WeakPasswordListPage(props: PageComponentProps) {
  const queryClient = useQueryClient();
  const confirmOperation = useOperationConfirm();
  const [editingId, setEditingId] = useState<string>();
  const [modalOpen, setModalOpen] = useState(false);
  const {
    records,
    total,
    pageNum,
    pageSize,
    query,
    onSearch,
    onPageChange,
    onRefresh,
    columnQueryProps,
  } = useListPageQuery({
    queryKey: weakPasswordQueryKeys.list({}),
    queryFn: weakPasswordApi.listPage,
  });
  const { selectedRowKeys, setSelectedRowKeys, selectedRecords, clearSelection } =
    useListSelection(records);
  const deleteMutation = useCommandMutation({
    mutationFn: weakPasswordApi.delete,
    successMessage: '删除成功',
    onSuccess: async () => {
      clearSelection();
      await queryClient.invalidateQueries({ queryKey: weakPasswordQueryKeys.all });
    },
  });
  const columns: ColumnsType<WeakPasswordVO> = [
    {
      title: '弱口令',
      dataIndex: 'word',
      width: 320,
      render: (word, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => {
            setEditingId(record.id);
            setModalOpen(true);
          }}
        >
          {word}
        </Button>
      ),
    },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  ];
  return (
    <>
      <ListPage<WeakPasswordVO>
        {...props}
        title="弱口令管理"
        access={weakPasswordAccess}
        loading={query.isLoading}
        error={getBlockingQueryError(query) as Error | null}
        onRetry={() => query.refetch()}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索弱口令"
        onAddNew={() => {
          setEditingId(undefined);
          setModalOpen(true);
        }}
        toolbarActions={[
          { builtin: 'add' },
          { builtin: 'refresh' },
          {
            key: 'delete',
            label: '删除',
            permission: weakPasswordAccess.permissions.delete,
            danger: true,
            disabled: selectedRecords.length !== 1,
            loading: deleteMutation.isPending,
            onClick: () => {
              const record = selectedRecords[0];
              if (!record) return;
              void confirmOperation({
                type: 'delete',
                title: '确认删除弱口令？',
                description: '删除后，此词条将不再参与弱口令拦截。',
                confirmText: '删除',
                onConfirm: () =>
                  deleteMutation.mutateAsync({ id: record.id, version: record.version }),
              });
            },
          },
        ]}
        onRefresh={() => {
          clearSelection();
          onRefresh();
        }}
        onQuickSearch={(keyword) => {
          clearSelection();
          onSearch(keyword);
        }}
        onPageChange={(nextPage, nextSize) => {
          clearSelection();
          onPageChange(nextPage, nextSize);
        }}
        rowKey="id"
        columns={columns}
        columnFeatures={columnFeatures}
        {...columnQueryProps}
        dataSource={records}
        selectMode="checkbox"
        selectedRowKeys={selectedRowKeys}
        onSelectChange={setSelectedRowKeys}
      />
      <WeakPasswordEditPage
        open={modalOpen}
        wordId={editingId}
        onClose={() => setModalOpen(false)}
      />
    </>
  );
}
