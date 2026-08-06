import { useState } from 'react';
import { Button, DatePicker, Select, Space, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { scriptApi } from './api';
import { scriptQueryKeys } from './queryKeys';
import type {
  ScriptLogListForm,
  ScriptLogListItem,
  ScriptStatus,
  ScriptTransactionMode,
} from './types';
import './scriptConsole.css';

const DETAIL_KEY = componentKeys.scriptLogDetail;
const statusColor = { SUCCESS: 'success', ERROR: 'error', TIMEOUT: 'warning' } as const;

export default function ScriptLogPage(props: PageComponentProps) {
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<ScriptStatus>();
  const [transactionMode, setTransactionMode] = useState<ScriptTransactionMode>();
  const [timeRange, setTimeRange] = useState<[Dayjs, Dayjs] | null>(null);
  const params: ScriptLogListForm = {
    pageNum,
    pageSize,
    keyword: keyword || undefined,
    status,
    transactionMode,
    startTime: timeRange?.[0].format('YYYY-MM-DDTHH:mm:ss'),
    endTime: timeRange?.[1].format('YYYY-MM-DDTHH:mm:ss'),
  };
  const query = useQuery({
    queryKey: scriptQueryKeys.logList(params),
    queryFn: () => scriptApi.logListPage(params),
  });
  const columns: ColumnsType<ScriptLogListItem> = [
    {
      title: '日志 ID',
      dataIndex: 'id',
      width: 210,
      fixed: 'left',
      render: (value, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(props.appNumber, DETAIL_KEY, '脚本执行详情', record.id, OperationType.VIEW)
          }
        >
          {value}
        </Button>
      ),
    },
    { title: '脚本', dataIndex: 'scriptName', render: (value) => value || '临时脚本' },
    {
      title: '状态',
      dataIndex: 'executeStatus',
      width: 100,
      render: (value: ScriptStatus) => <Tag color={statusColor[value]}>{value}</Tag>,
    },
    { title: '执行模式', dataIndex: 'transactionMode', width: 130 },
    { title: '事务结果', dataIndex: 'transactionResult', width: 140 },
    { title: '耗时', dataIndex: 'executeDuration', width: 100, render: (value) => `${value} ms` },
    { title: '执行人', dataIndex: 'createName', width: 130 },
    { title: 'IP', dataIndex: 'createIp', width: 140 },
    { title: '执行时间', dataIndex: 'createTime', width: 180 },
  ];
  return (
    <ListPage<ScriptLogListItem>
      {...props}
      title="脚本执行历史"
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={query.data?.total ?? 0}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索脚本名称/内容"
      filterSummary={
        [status, transactionMode, timeRange ? '已设置时间范围' : undefined]
          .filter(Boolean)
          .join(' / ') || undefined
      }
      filterContent={
        <Space wrap>
          <Select
            allowClear
            className="sm-script-log-filter"
            placeholder="执行状态"
            value={status}
            options={['SUCCESS', 'ERROR', 'TIMEOUT'].map((value) => ({ value, label: value }))}
            onChange={(value) => {
              setStatus(value);
              setPageNum(1);
            }}
          />
          <Select
            allowClear
            className="sm-script-log-filter"
            placeholder="事务模式"
            value={transactionMode}
            options={[
              { value: 'ATOMIC', label: '原子事务' },
              { value: 'NON_ATOMIC', label: '非事务' },
            ]}
            onChange={(value) => {
              setTransactionMode(value);
              setPageNum(1);
            }}
          />
          <DatePicker.RangePicker
            showTime
            value={timeRange}
            onChange={(value) => {
              setTimeRange(value as [Dayjs, Dayjs] | null);
              setPageNum(1);
            }}
          />
        </Space>
      }
      onQuickSearch={(value) => {
        setKeyword(value);
        setPageNum(1);
      }}
      onRefresh={() => query.refetch()}
      onPageChange={(nextPage, nextSize) => {
        setPageNum(nextPage);
        setPageSize(nextSize);
      }}
      rowKey="id"
      columns={columns}
      dataSource={query.data?.records ?? []}
    />
  );
}
