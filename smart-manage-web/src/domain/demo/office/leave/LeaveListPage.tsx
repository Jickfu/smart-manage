import { useState } from 'react';
import { Button } from 'antd';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { componentKeys } from '../../componentKeys';
import { leaveApi } from './api';
import type { LeaveDetail } from './api';
import { leaveAccess } from './permissions';

export default function LeaveListPage(props: PageComponentProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [selection, setSelection] = useState<React.Key[]>([]);
  const confirm = useOperationConfirm();
  const query = useQuery({
    queryKey: ['demo', 'leave', 'list', pageNum, pageSize],
    queryFn: () => leaveApi.listPage({ pageNum, pageSize }),
    enabled: props.active,
    meta: { errorPresentation: 'local-initial' },
  });
  const remove = useCommandMutation({
    mutationFn: async () => {
      for (const row of query.data?.records ?? [])
        if (selection.includes(row.id)) await leaveApi.delete(row.id, row.version);
    },
    successMessage: '草稿已删除',
    onSuccess: async () => {
      setSelection([]);
      await query.refetch();
    },
  });
  return (
    <ListPage<LeaveDetail>
      title="请假申请"
      access={leaveAccess}
      rowKey="id"
      loading={query.isLoading}
      error={getBlockingQueryError(query)}
      onRetry={() => {
        if (props.active) void query.refetch();
      }}
      dataSource={query.data?.records ?? []}
      total={query.data?.total}
      pageNum={pageNum}
      pageSize={pageSize}
      onPageChange={(page, size) => {
        setPageNum(page);
        setPageSize(size);
      }}
      onRefresh={() => {
        if (props.active) void query.refetch();
      }}
      onAddNew={() =>
        useWorkbenchStore.getState().openAddNewTab(props.appNumber, componentKeys.leaveEdit)
      }
      onDelete={() => {
        if (selection.length)
          void confirm({
            type: 'delete',
            title: '删除请假草稿',
            description: '仅可删除从未提交过的草稿。',
            confirmText: '删除',
            onConfirm: () => remove.mutateAsync(),
          });
      }}
      selectMode="checkbox"
      selectedRowKeys={selection}
      onSelectChange={setSelection}
      isRowSelectable={(row) => row.billStatus === 'A' && !row.currentInstanceId}
      columns={[
        {
          title: '单据编号',
          dataIndex: 'number',
          width: 190,
          render: (value: string, row) => (
            <Button
              type="link"
              onClick={() =>
                useWorkbenchStore
                  .getState()
                  .openBillTab(
                    props.appNumber,
                    componentKeys.leaveEdit,
                    row.id,
                    row.billStatus === 'A' ? OperationType.EDIT : OperationType.VIEW,
                  )
              }
            >
              {value}
            </Button>
          ),
        },
        { title: '事由', dataIndex: 'reason' },
        { title: '开始时间', dataIndex: 'startTime', width: 180 },
        { title: '结束时间', dataIndex: 'endTime', width: 180 },
        { title: '天数', dataIndex: 'days', width: 80 },
        {
          title: '状态',
          dataIndex: 'billStatus',
          width: 100,
          render: (value: LeaveDetail['billStatus']) =>
            ({ A: '草稿', B: '审批中', C: '审批通过' })[value],
        },
      ]}
    />
  );
}
