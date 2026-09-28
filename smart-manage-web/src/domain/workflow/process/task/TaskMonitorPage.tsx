import { useState } from 'react';
import { Button } from 'antd';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { componentKeys } from '../../componentKeys';
import { workflowApi } from '../api';
import type { TaskMonitorRow } from '../api';

export default function TaskMonitorPage(props: PageComponentProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const query = useQuery({
    queryKey: ['workflow', 'task-monitor', pageNum, pageSize, keyword],
    queryFn: () => workflowApi.taskMonitor({ pageNum, pageSize, keyword: keyword || undefined }),
    enabled: props.active,
    meta: { errorPresentation: 'local-initial' },
  });
  const open = (id: string) =>
    useWorkbenchStore
      .getState()
      .openBillTab(props.appNumber, componentKeys.instanceDetail, id, OperationType.VIEW);
  return (
    <ListPage<TaskMonitorRow>
      title="任务监控"
      rowKey="id"
      dataSource={query.data?.records ?? []}
      total={query.data?.total}
      pageNum={pageNum}
      pageSize={pageSize}
      loading={query.isLoading}
      error={getBlockingQueryError(query)}
      onRetry={() => props.active && void query.refetch()}
      quickSearchPlaceholder="搜索单据编号/业务类型/节点"
      filterSummary={keyword ? `关键字：${keyword}` : undefined}
      onQuickSearch={(value) => {
        setKeyword(value.trim());
        setPageNum(1);
      }}
      onPageChange={(page, size) => {
        setPageNum(page);
        setPageSize(size);
      }}
      onRefresh={() => props.active && void query.refetch()}
      toolbarActions={[{ builtin: 'refresh' }]}
      columns={[
        {
          title: '单据编号',
          dataIndex: 'number',
          width: 180,
          render: (value: string, row) => (
            <Button type="link" onClick={() => open(row.instanceId)}>
              {value}
            </Button>
          ),
        },
        { title: '业务类型', dataIndex: 'businessType', width: 200 },
        { title: '任务节点', dataIndex: 'nodeName' },
        {
          title: '候选人',
          dataIndex: 'candidates',
          render: (values: string[], row) =>
            values.map((id) => row.candidateNames[id] ?? id).join('、'),
        },
        {
          title: '运行状态',
          dataIndex: 'active',
          width: 100,
          render: (value: boolean) => (value ? '活动' : '挂起'),
        },
        { title: '到达时间', dataIndex: 'createTime', width: 180 },
      ]}
    />
  );
}
