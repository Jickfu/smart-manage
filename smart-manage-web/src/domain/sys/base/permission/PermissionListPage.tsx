import { useState, useCallback } from 'react';
import { App, Button } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/ListPage';
import { useListPageQuery } from '@/domain/common/page/useListPageQuery';
import { usePermissionAccess } from '@/domain/common/page/usePermissionAccess';
import { usePermissionDeleteMutation } from './usePermissionDeleteMutation';
import { permissionApi } from './api';
import { permissionQueryKeys } from './queryKeys';
import type { PermissionListVO } from './types';
import type { PageComponentProps } from '@/domain/common/page/types';
import PermissionEditPage from './PermissionEditPage';
import { permissionAccess } from './permissions';

/** 权限管理列表页 */
const PermissionListPage = (props: PageComponentProps) => {
  const { modal } = App.useApp();
  const { can } = usePermissionAccess(permissionAccess.prefix);
  const { records, total, pageNum, pageSize, keyword, query, onSearch, onPageChange, onRefresh } =
    useListPageQuery({
      queryKey: permissionQueryKeys.lists(),
      queryFn: (params) => permissionApi.listPage(params),
    });

  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [editId, setEditId] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const deleteMutation = usePermissionDeleteMutation(async () => {
    setSelectedRowKeys([]);
    await query.refetch();
  });

  const handleOpenEdit = useCallback((id: string) => {
    setEditId(id);
    setModalOpen(true);
  }, []);

  const handleOpenAdd = useCallback(() => {
    setEditId(null);
    setModalOpen(true);
  }, []);

  const handleModalClose = useCallback(() => {
    setModalOpen(false);
  }, []);

  const handleSaved = useCallback(() => {
    query.refetch();
  }, [query]);

  const handleDelete = useCallback(() => {
    if (selectedRowKeys.length === 0) return;
    modal.confirm({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedRowKeys.length} 条记录吗？`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => deleteMutation.mutateAsync(selectedRowKeys.map(String)),
    });
  }, [selectedRowKeys, deleteMutation, modal]);

  const columns: ColumnsType<PermissionListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 220,
      render: (text, record) =>
        can(permissionAccess.permissions.detail) ? (
          <Button type="link" size="small" onClick={() => handleOpenEdit(record.id)}>
            {text}
          </Button>
        ) : (
          text
        ),
    },
    { title: '名称', dataIndex: 'name' },
  ];

  return (
    <>
      <ListPage<PermissionListVO>
        {...props}
        title="权限管理"
        access={permissionAccess}
        loading={query.isLoading}
        error={query.error as Error | null}
        onRetry={() => query.refetch()}
        total={total}
        pageNum={pageNum}
        pageSize={pageSize}
        quickSearchPlaceholder="搜索编码/名称"
        filterSummary={keyword ? `关键字：${keyword}` : undefined}
        onAddNew={handleOpenAdd}
        onDelete={handleDelete}
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
      <PermissionEditPage
        open={modalOpen}
        permissionId={editId}
        onClose={handleModalClose}
        onSaved={handleSaved}
      />
    </>
  );
};

export default PermissionListPage;
