import { useState } from 'react';
import { Button, Select } from 'antd';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { DomainViewProps } from '@/domain/common/registry/domainExtensions';
import { workflowApi, stateLabels } from '../api';
import type { TaskBox, TaskRow } from '../api';
import ApprovalView from './ApprovalView';
import CandidateMaintenance from './CandidateMaintenance';
import '../workflow.css';

const boxes = [
  { value: 'PENDING', label: '待办' },
  { value: 'COMPLETED', label: '已办' },
  { value: 'STARTED', label: '我发起' },
];
export default function TaskCenter({ active, context, onDirtyChange }: DomainViewProps) {
  const [selectedBox, setBox] = useState<TaskBox>('PENDING');
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [instanceId, setInstanceId] = useState<string>();
  const box: TaskBox =
    context?.category === 'task-completed'
      ? 'COMPLETED'
      : context?.category === 'task-started'
        ? 'STARTED'
        : context?.category === 'task-pending'
          ? 'PENDING'
          : selectedBox;
  const query = useQuery({
    queryKey: ['workflow', 'tasks', box, pageNum, pageSize],
    queryFn: () => workflowApi.tasks({ box, pageNum, pageSize }),
    enabled: active,
    meta: { errorPresentation: 'local-initial' },
  });
  if (instanceId)
    return (
      <ApprovalView
        resourceId={instanceId}
        context={context}
        active={active}
        onDirtyChange={onDirtyChange}
        onBack={() => {
          onDirtyChange?.(false);
          setInstanceId(undefined);
          void query.refetch();
        }}
      />
    );
  return (
    <div className="sm-workflow-view">
      <ListPage<TaskRow>
        title="任务中心"
        rowKey="id"
        dataSource={query.data?.records ?? []}
        total={query.data?.total}
        pageNum={pageNum}
        pageSize={pageSize}
        loading={query.isLoading}
        error={getBlockingQueryError(query)}
        onRetry={() => {
          if (active) void query.refetch();
        }}
        onPageChange={(page, size) => {
          setPageNum(page);
          setPageSize(size);
        }}
        onRefresh={() => {
          if (active) void query.refetch();
        }}
        toolbarActions={[{ builtin: 'refresh' }]}
        toolbarExtra={
          <>
            <Select
              aria-label="任务分类"
              value={box}
              options={boxes}
              disabled={Boolean(context?.category)}
              onChange={(value) => {
                setBox(value);
                setPageNum(1);
              }}
            />
            <CandidateMaintenance />
          </>
        }
        columns={[
          {
            title: '单据编号',
            dataIndex: 'number',
            render: (value: string, row) => (
              <Button type="link" onClick={() => setInstanceId(row.id)}>
                {value}
              </Button>
            ),
          },
          { title: '流程实例', dataIndex: 'id', width: 190 },
          { title: '当前节点', dataIndex: 'currentNode', width: 180 },
          {
            title: '状态',
            dataIndex: 'state',
            width: 100,
            render: (state: TaskRow['state']) => stateLabels[state],
          },
        ]}
      />
    </div>
  );
}
