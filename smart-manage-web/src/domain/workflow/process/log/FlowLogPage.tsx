import { useState } from 'react';
import { Button } from 'antd';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import type { PageComponentProps } from '@/domain/common/page/types';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { componentKeys } from '../../componentKeys';
import { workflowActionLabels, workflowApi } from '../api';
import type { FlowLogRow } from '../api';

export default function FlowLogPage(props: PageComponentProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const query = useQuery({
    queryKey: ['workflow', 'flow-logs', pageNum, pageSize, keyword],
    queryFn: () => workflowApi.flowLogs({ pageNum, pageSize, keyword: keyword || undefined }),
    enabled: props.active,
    meta: { errorPresentation: 'local-initial' },
  });
  return (
    <ListPage<FlowLogRow>
      title="流转日志"
      rowKey="id"
      dataSource={query.data?.records ?? []}
      total={query.data?.total}
      pageNum={pageNum}
      pageSize={pageSize}
      loading={query.isLoading}
      error={getBlockingQueryError(query)}
      onRetry={() => props.active && void query.refetch()}
      quickSearchPlaceholder="搜索单据编号/业务类型/节点/动作"
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
            <Button
              type="link"
              onClick={() =>
                useWorkbenchStore
                  .getState()
                  .openBillTab(
                    props.appNumber,
                    componentKeys.instanceDetail,
                    row.instanceId,
                    OperationType.VIEW,
                  )
              }
            >
              {value}
            </Button>
          ),
        },
        { title: '业务类型', dataIndex: 'businessType', width: 180 },
        { title: '节点', dataIndex: 'nodeName', width: 160 },
        {
          title: '动作',
          dataIndex: 'action',
          width: 140,
          render: (value: string) => workflowActionLabels[value] ?? value,
        },
        {
          title: '操作人',
          dataIndex: 'operatorName',
          width: 140,
          render: (value: string | undefined, row) => value ?? row.operatorId ?? '-',
        },
        { title: '意见/原因', dataIndex: 'opinion' },
        {
          title: '来源',
          dataIndex: 'source',
          width: 100,
          render: (value: FlowLogRow['source']) =>
            value === 'ENGINE' ? '流程引擎' : value === 'SCRIPT' ? '脚本节点' : '管理操作',
        },
        { title: '时间', dataIndex: 'eventTime', width: 180 },
      ]}
    />
  );
}
