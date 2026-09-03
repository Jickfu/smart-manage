import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { useState } from 'react';
import { Button, Select, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQueryClient } from '@tanstack/react-query';
import { useCommandMutation } from '@/domain/common/page/command/useCommandMutation';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import { OperationType } from '@/domain/common/page/types';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import './JobListPage.css';
import { useWorkbenchStore } from '@/stores/workbench';
import { jobApi } from './api';
import { jobAccess } from './permissions';
import { jobQueryKeys } from './queryKeys';
import type { JobStatus, JobVO } from './types';
import type { ListColumnFeatures } from '@/domain/common/page/list/listQuery';
import { useOperationConfirm } from '@/domain/common/component/useOperationConfirm';

const EDIT_KEY = componentKeys.schedulerJobEdit;

const columnFeatures: ListColumnFeatures = {
  number: { label: '任务编码', filter: { type: 'string' }, sorter: true },
  jobName: { label: '任务名称', filter: { type: 'string' }, sorter: true },
  jobGroup: { label: '分组', filter: { type: 'string' }, sorter: true },
  cronExpression: { label: 'Cron 表达式', filter: { type: 'string' } },
  status: {
    label: '状态',
    filter: {
      type: 'enum',
      options: [
        { label: '已启用', value: 'ENABLED' },
        { label: '已暂停', value: 'PAUSED' },
      ],
    },
    sorter: true,
  },
  jobClassName: { label: '执行类', filter: { type: 'string' } },
};

const JobListPage = (props: PageComponentProps) => {
  const feedback = useOperationFeedback();
  const confirmOperation = useOperationConfirm();
  const [status, setStatus] = useState<JobStatus>();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const openAddNewTab = useWorkbenchStore((state) => state.openAddNewTab);
  const queryClient = useQueryClient();
  const list = useListPageQuery({
    queryKey: jobQueryKeys.list(status),
    queryFn: (params) => jobApi.listPage({ ...params, status }),
  });
  const selectedRecords = list.records.filter((record) => selectedRowKeys.includes(record.id));
  const selected = selectedRecords.length === 1 ? selectedRecords[0] : undefined;
  const refreshAll = async (successMessage: string) => {
    setSelectedRowKeys([]);
    await queryClient.invalidateQueries({ queryKey: jobQueryKeys.all });
    feedback.success(successMessage);
  };
  const commandMutation = useCommandMutation({
    mutationFn: async (command: 'delete' | 'pause' | 'resume' | 'trigger' | 'sync') => {
      if (command === 'sync') return jobApi.syncAll();
      if (command === 'pause' || command === 'resume') {
        if (selectedRecords.length === 0) throw new Error('请至少选择一个任务');
        return jobApi[command](
          selectedRecords.map((record) => ({ id: record.id, version: record.version })),
        );
      }
      if (!selected) throw new Error('该操作只能选择一个任务');
      return command === 'trigger'
        ? jobApi.trigger(selected.id)
        : jobApi.delete(selected.id, selected.version);
    },
    onSuccess: (_, command) =>
      refreshAll(
        {
          delete: '删除成功',
          pause: '暂停成功',
          resume: '恢复成功',
          trigger: '触发成功',
          sync: '同步成功',
        }[command],
      ),
  });
  const columns: ColumnsType<JobVO> = [
    {
      title: '任务编码',
      dataIndex: 'number',
      width: 180,
      fixed: 'left',
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
    { title: '任务名称', dataIndex: 'jobName', width: 180 },
    { title: '分组', dataIndex: 'jobGroup', width: 140 },
    { title: 'Cron 表达式', dataIndex: 'cronExpression', width: 180 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (value: JobStatus) =>
        value === 'ENABLED' ? <Tag color="success">已启用</Tag> : <Tag>已暂停</Tag>,
    },
    { title: '上次执行时间', dataIndex: 'lastExecuteTime', width: 180 },
    { title: '上次执行结果', dataIndex: 'lastExecuteStatus', width: 120 },
    { title: '执行类', dataIndex: 'jobClassName', ellipsis: true },
  ];

  const confirmCommand = (command: 'delete' | 'trigger', title: string) => {
    if (!selected) return;
    void confirmOperation({
      type: command === 'delete' ? 'delete' : 'normal',
      title,
      description: `${selected.jobGroup} / ${selected.jobName}`,
      confirmText: command === 'delete' ? '删除' : '确定',
      onConfirm: () => commandMutation.mutateAsync(command),
    });
  };

  return (
    <ListPage<JobVO>
      {...props}
      title="定时任务"
      access={jobAccess}
      loading={list.query.isLoading}
      error={getBlockingQueryError(list.query) as Error | null}
      onRetry={() => list.query.refetch()}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      quickSearchPlaceholder="搜索任务编码、名称或分组"
      filterContent={
        <Select
          allowClear
          placeholder="全部状态"
          value={status}
          options={[
            { label: '已启用', value: 'ENABLED' },
            { label: '已暂停', value: 'PAUSED' },
          ]}
          className="sm-job-status-filter"
          onChange={(value) => setStatus(value)}
        />
      }
      filterSummary={status ? `状态：${status === 'ENABLED' ? '已启用' : '已暂停'}` : undefined}
      onAddNew={() => openAddNewTab(props.appNumber, EDIT_KEY)}
      toolbarActions={[
        {
          key: 'pause',
          label: '暂停',
          permission: jobAccess.permissions.save,
          disabled:
            selectedRecords.length === 0 ||
            selectedRecords.some((record) => record.status !== 'ENABLED'),
          loading: commandMutation.isPending,
          onClick: () => commandMutation.mutate('pause'),
        },
        {
          key: 'resume',
          label: '恢复',
          permission: jobAccess.permissions.save,
          disabled:
            selectedRecords.length === 0 ||
            selectedRecords.some((record) => record.status !== 'PAUSED'),
          loading: commandMutation.isPending,
          onClick: () => commandMutation.mutate('resume'),
        },
        {
          key: 'trigger',
          label: '立即执行',
          permission: jobAccess.permissions.save,
          disabled: selectedRecords.length !== 1,
          loading: commandMutation.isPending,
          onClick: () => confirmCommand('trigger', '确认立即执行该任务？'),
        },
        {
          key: 'sync',
          label: '重新同步',
          permission: jobAccess.permissions.save,
          loading: commandMutation.isPending,
          onClick: () => commandMutation.mutate('sync'),
        },
        {
          key: 'delete',
          label: '删除',
          permission: jobAccess.permissions.delete,
          danger: true,
          disabled: selectedRecords.length !== 1 || selected?.isSystem,
          loading: commandMutation.isPending,
          onClick: () => confirmCommand('delete', '确认删除该任务？'),
        },
      ]}
      onRefresh={list.onRefresh}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      columnFeatures={columnFeatures}
      {...list.columnQueryProps}
      dataSource={list.records}
      selectMode="checkbox"
      selectedRowKeys={selectedRowKeys}
      onSelectChange={setSelectedRowKeys}
    />
  );
};

export default JobListPage;
