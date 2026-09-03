import { useState } from 'react';
import { Button, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ListPage from '@/domain/common/page/list/ListPage';
import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { BillStatus, OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { purchaseRequisitionApi } from './api';
import { purchaseRequisitionAccess } from './permissions';
import { purchaseRequisitionQueryKeys } from './queryKeys';
import type { PurchaseRequisitionListVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { serializeListFilters } from '@/domain/common/page/list/listQuery';
import { useArtifactExport } from '@/domain/common/dataExchange/useArtifactExport';
import { DataExchangeActions } from '@/domain/common/dataExchange/DataExchangeActions';

const EDIT_COMPONENT_KEY = componentKeys.purchaseRequisitionEdit;

const statusView = {
  [BillStatus.SAVED]: { label: '暂存', color: 'default' },
  [BillStatus.SUBMITTED]: { label: '已提交', color: 'blue' },
  [BillStatus.AUDITED]: { label: '审核通过', color: 'green' },
  [BillStatus.CLOSED]: { label: '已关闭', color: 'default' },
} as const;

const columnFeatures: ListColumnFeatures = {
  number: { label: '编码', filter: { type: 'string' }, sorter: true },
  subject: { label: '主题', filter: { type: 'string' } },
  bizDate: { label: '业务日期', filter: { type: 'date' }, sorter: true },
  requiredDate: { label: '需求日期', filter: { type: 'date' }, sorter: true },
  billStatus: {
    label: '单据状态',
    filter: {
      type: 'enum',
      options: Object.entries(statusView).map(([value, view]) => ({ value, label: view.label })),
    },
  },
  createTime: { label: '创建时间', filter: { type: 'date' }, sorter: true },
};

const PurchaseRequisitionListPage = (props: PageComponentProps) => {
  const confirmOperation = useOperationConfirm();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const listQuery = useListPageQuery({
    queryKey: purchaseRequisitionQueryKeys.list({}),
    queryFn: purchaseRequisitionApi.listPage,
  });
  const deleteMutation = useCommandMutation({
    mutationFn: (records: PurchaseRequisitionListVO[]) =>
      Promise.all(
        records.map((record) =>
          purchaseRequisitionApi.delete({ id: record.id, version: record.version }),
        ),
      ),
    successMessage: '删除成功',
    onSuccess: async () => {
      setSelectedRowKeys([]);
      await listQuery.query.refetch();
    },
  });
  const exportMutation = useArtifactExport(purchaseRequisitionApi.export, '采购申请导出完成');

  const openDetail = (record: PurchaseRequisitionListVO) => {
    const operationType =
      record.billStatus === BillStatus.SAVED ? OperationType.EDIT : OperationType.VIEW;
    openBillTab(props.appNumber, EDIT_COMPONENT_KEY, record.id, operationType);
  };

  const columns: ColumnsType<PurchaseRequisitionListVO> = [
    {
      title: '编码',
      dataIndex: 'number',
      width: 180,
      render: (value, record) => (
        <Button type="link" size="small" onClick={() => openDetail(record)}>
          {value}
        </Button>
      ),
    },
    { title: '主题', dataIndex: 'subject' },
    { title: '业务日期', dataIndex: 'bizDate', width: 120 },
    { title: '需求日期', dataIndex: 'requiredDate', width: 120 },
    {
      title: '单据状态',
      dataIndex: 'billStatus',
      width: 100,
      render: (value: BillStatus) => {
        const view = statusView[value];
        return view ? <Tag color={view.color}>{view.label}</Tag> : value;
      },
    },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];

  const confirmDelete = () => {
    const selectedIds = new Set(selectedRowKeys.map(String));
    const records = listQuery.records.filter((record) => selectedIds.has(record.id));
    if (records.length === 0) return;
    void confirmOperation({
      type: 'delete',
      title: '删除采购申请',
      description: '只有暂存状态的采购申请可以删除，是否继续？',
      confirmText: '删除',
      onConfirm: () => deleteMutation.mutateAsync(records),
    });
  };

  return (
    <ListPage<PurchaseRequisitionListVO>
      {...props}
      title="采购申请"
      access={purchaseRequisitionAccess}
      loading={listQuery.query.isLoading}
      error={listQuery.query.error as Error | null}
      onRetry={() => listQuery.query.refetch()}
      total={listQuery.total}
      pageNum={listQuery.pageNum}
      pageSize={listQuery.pageSize}
      quickSearchPlaceholder="搜索编码/主题"
      filterSummary={listQuery.keyword ? `关键字：${listQuery.keyword}` : undefined}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_COMPONENT_KEY)}
      onDelete={confirmDelete}
      onRefresh={listQuery.onRefresh}
      onQuickSearch={listQuery.onSearch}
      onPageChange={listQuery.onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...listQuery.columnQueryProps}
      dataSource={listQuery.records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
      toolbarExtra={
        <DataExchangeActions
          permissionPrefix={purchaseRequisitionAccess.prefix}
          exportPermission={purchaseRequisitionAccess.permissions.export}
          exporting={exportMutation.isPending}
          onExport={(layout) => {
            exportMutation.mutate({
              pageNum: 1,
              pageSize: 10000,
              keyword: listQuery.keyword || undefined,
              filters: serializeListFilters(listQuery.columnFilters),
              sortField: listQuery.columnSort?.field,
              sortOrder: listQuery.columnSort?.order,
              ids: selectedRowKeys.length ? selectedRowKeys.map(String) : undefined,
              layout,
            });
          }}
        />
      }
    />
  );
};

export default PurchaseRequisitionListPage;
