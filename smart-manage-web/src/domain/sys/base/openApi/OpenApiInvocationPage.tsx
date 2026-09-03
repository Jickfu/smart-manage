import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useState } from 'react';
import { Select, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import ListPage from '@/domain/common/page/list/ListPage';
import { useListPageQuery } from '@/domain/common/page/list/useListPageQuery';
import type { PageComponentProps } from '@/domain/common/page/types';
import { openApiPlatformApi } from './api';
import { openApiInvocationAccess } from './permissions';
import { openApiQueryKeys } from './queryKeys';
import type { OpenApiInvocation } from './types';
import './OpenApiPage.css';

const resultOptions = [
  { label: '成功', value: 'SUCCESS' },
  { label: '认证失败', value: 'AUTHENTICATION_FAILED' },
  { label: '访问拒绝', value: 'ACCESS_DENIED' },
  { label: '业务失败', value: 'BUSINESS_FAILED' },
  { label: '系统失败', value: 'SYSTEM_FAILED' },
];

const OpenApiInvocationPage = (props: PageComponentProps) => {
  const [resultType, setResultType] = useState<string>();
  const list = useListPageQuery({
    queryKey: openApiQueryKeys.invocationList({ resultType }),
    queryFn: (params) => openApiPlatformApi.invocationList({ ...params, resultType }),
  });
  const stats = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: openApiQueryKeys.stats(),
    queryFn: openApiPlatformApi.invocationStats,
    refetchInterval: 60_000,
  });
  const summary = stats.data?.summary ?? {};
  const columns: ColumnsType<OpenApiInvocation> = [
    { title: '请求时间', dataIndex: 'requestTime', width: 180 },
    {
      title: '第三方系统',
      dataIndex: 'applicationNumber',
      width: 170,
      render: (value) => value ?? '未知',
    },
    {
      title: '操作标识',
      dataIndex: 'operationKey',
      width: 280,
      render: (value) => value ?? '未知',
    },
    { title: 'Request ID', dataIndex: 'requestId', width: 230 },
    { title: '客户端 IP', dataIndex: 'clientIp', width: 150 },
    {
      title: '结果',
      dataIndex: 'resultType',
      width: 120,
      render: (value: string) => (
        <Tag
          color={
            value === 'SUCCESS' ? 'success' : value === 'BUSINESS_FAILED' ? 'warning' : 'error'
          }
        >
          {resultOptions.find((option) => option.value === value)?.label ?? value}
        </Tag>
      ),
    },
    { title: '结果码', dataIndex: 'resultCode', width: 100 },
    { title: '耗时', dataIndex: 'durationMs', width: 100, render: (value) => `${value} ms` },
    { title: '请求字节', dataIndex: 'requestBytes', width: 110 },
    { title: '响应字节', dataIndex: 'responseBytes', width: 110 },
    { title: '错误信息', dataIndex: 'errorMessage', ellipsis: true },
  ];
  return (
    <ListPage<OpenApiInvocation>
      {...props}
      title="调用监控"
      access={openApiInvocationAccess}
      loading={list.query.isLoading || stats.isLoading}
      error={(getBlockingQueryError(list.query) ?? getBlockingQueryError(stats)) as Error | null}
      onRetry={() => Promise.all([list.query.refetch(), stats.refetch()])}
      total={list.total}
      pageNum={list.pageNum}
      pageSize={list.pageSize}
      filterContent={
        <div className="sm-openapi-stats">
          <span>近 24 小时调用：{Number(summary.total_count ?? 0)}</span>
          <span>成功：{Number(summary.success_count ?? 0)}</span>
          <span>平均耗时：{Number(summary.average_duration_ms ?? 0)} ms</span>
          <span>活跃应用：{Number(summary.application_count ?? 0)}</span>
          <Select
            allowClear
            placeholder="全部结果"
            value={resultType}
            options={resultOptions}
            onChange={setResultType}
          />
        </div>
      }
      onRefresh={() => Promise.all([list.onRefresh(), stats.refetch()])}
      onQuickSearch={list.onSearch}
      onPageChange={list.onPageChange}
      rowKey="id"
      columns={columns}
      dataSource={list.records}
    />
  );
};

export default OpenApiInvocationPage;
