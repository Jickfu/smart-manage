import { useState } from 'react';
import { Button, DatePicker, Select, Space, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import './SqlLogPage.css';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/ListPage';
import type { PageComponentProps } from '@/domain/common/page/types';
import { componentKeys } from '@/domain/common/registry/componentKeys';
import { OperationType } from '@/domain/common/page/types';
import { useWorkbenchStore } from '@/stores/workbench';
import { sqlApi } from './api';
import { sqlQueryKeys } from './queryKeys';
import type { SqlLogListForm, SqlLogListItem, SqlResultType } from './types';

const DETAIL_KEY = componentKeys.sqlLogDetail;

export default function SqlLogPage(props: PageComponentProps) {
  const openBillTab = useWorkbenchStore((state) => state.openBillTab);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [keyword, setKeyword] = useState('');
  const [resultType, setResultType] = useState<SqlResultType>();
  const [timeRange, setTimeRange] = useState<[Dayjs, Dayjs] | null>(null);
  const params: SqlLogListForm = {
    pageNum,
    pageSize,
    keyword: keyword || undefined,
    resultType,
    startTime: timeRange?.[0].format('YYYY-MM-DDTHH:mm:ss'),
    endTime: timeRange?.[1].format('YYYY-MM-DDTHH:mm:ss'),
  };
  const query = useQuery({
    queryKey: sqlQueryKeys.logList(params),
    queryFn: () => sqlApi.listPage(params),
  });
  const columns: ColumnsType<SqlLogListItem> = [
    {
      title: '日志 ID',
      dataIndex: 'id',
      width: 210,
      fixed: 'left',
      render: (value: string, record) => (
        <Button
          type="link"
          size="small"
          onClick={() =>
            openBillTab(props.appNumber, DETAIL_KEY, 'SQL 执行详情', record.id, OperationType.VIEW)
          }
        >
          {value}
        </Button>
      ),
    },
    {
      title: 'SQL',
      dataIndex: 'sqlText',
      ellipsis: true,
      render: (value: string) => <Typography.Text code>{value}</Typography.Text>,
    },
    {
      title: '结果',
      dataIndex: 'resultType',
      width: 100,
      render: (value) => <Tag color={value === 'ERROR' ? 'error' : 'success'}>{value}</Tag>,
    },
    { title: '行数', dataIndex: 'rowCount', width: 90 },
    { title: '耗时', dataIndex: 'executeDuration', width: 100, render: (value) => `${value} ms` },
    { title: '执行人', dataIndex: 'createName', width: 130 },
    { title: 'IP', dataIndex: 'createIp', width: 140 },
    { title: '执行时间', dataIndex: 'createTime', width: 180 },
  ];
  return (
    <ListPage<SqlLogListItem>
      {...props}
      title="SQL 执行历史"
      loading={query.isLoading}
      error={query.error as Error | null}
      onRetry={() => query.refetch()}
      total={query.data?.total ?? 0}
      pageNum={pageNum}
      pageSize={pageSize}
      quickSearchPlaceholder="搜索 SQL 文本"
      filterSummary={
        [resultType, timeRange ? '已设置时间范围' : undefined].filter(Boolean).join(' / ') ||
        undefined
      }
      filterContent={
        <Space wrap>
          <Select
            allowClear
            placeholder="执行结果"
            className="sm-sql-log-result-filter"
            value={resultType}
            options={['QUERY', 'DML', 'DDL', 'ERROR'].map((value) => ({ value, label: value }))}
            onChange={(value) => {
              setResultType(value);
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
