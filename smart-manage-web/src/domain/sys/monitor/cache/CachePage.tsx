import { useMemo } from 'react';
import { Alert, Button, Card, Descriptions, Statistic, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import SmChart from '@/domain/common/chart/SmChart';
import type { PageComponentProps } from '@/domain/common/page/types';
import { redisApi } from '../redis/api';
import { redisQueryKeys } from '../redis/queryKeys';
import { cacheApi } from './api';
import { cacheQueryKeys } from './queryKeys';
import type { ManagedCache } from './types';
import './cachePage.css';

export default function CachePage(_: PageComponentProps) {
  const cacheQuery = useQuery({ queryKey: cacheQueryKeys.overview(), queryFn: cacheApi.overview });
  const runtimeQuery = useQuery({ queryKey: redisQueryKeys.runtime(), queryFn: redisApi.runtime });
  const memoryOption = useMemo(() => {
    const used = runtimeQuery.data?.usedMemoryBytes ?? 0;
    const maximum = runtimeQuery.data?.maxMemoryBytes ?? 0;
    const data =
      maximum > 0
        ? [
            { name: '已使用', value: used },
            { name: '可用', value: Math.max(maximum - used, 0) },
          ]
        : [{ name: '已使用（未设上限）', value: Math.max(used, 1) }];
    return {
      tooltip: { trigger: 'item', valueFormatter: (value: number) => `${value} B` },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['48%', '70%'],
          center: ['50%', '43%'],
          label: { show: false },
          data,
        },
      ],
    };
  }, [runtimeQuery.data]);
  const columns: ColumnsType<ManagedCache> = [
    {
      title: '缓存',
      dataIndex: 'displayName',
      render: (value, record) => (
        <>
          <strong>{value}</strong>
          <div className="sm-cache-code">{record.name}</div>
        </>
      ),
    },
    { title: '存储', dataIndex: 'type', width: 90, render: (value) => <Tag>{value}</Tag> },
    { title: '读取', dataIndex: 'getCount', width: 90 },
    { title: '命中', dataIndex: 'hitCount', width: 90 },
    { title: '未命中', dataIndex: 'missCount', width: 90 },
    { title: '失败', dataIndex: 'failCount', width: 80 },
    {
      title: '命中率',
      dataIndex: 'hitRate',
      width: 100,
      render: (value: number) => `${(value * 100).toFixed(1)}%`,
    },
    { title: 'QPS', dataIndex: 'qps', width: 90, render: (value: number) => value.toFixed(2) },
    {
      title: '平均耗时',
      dataIndex: 'averageGetTime',
      width: 110,
      render: (value: number) => `${value.toFixed(2)} ms`,
    },
    { title: '条目数', dataIndex: 'estimatedSize', width: 90, render: (value) => value ?? '-' },
  ];
  const error = cacheQuery.error ?? runtimeQuery.error;
  return (
    <div className="sm-cache-page">
      {error && <Alert type="error" showIcon title={error.message} />}
      <div className="sm-cache-status-toolbar">
        <h2>缓存状态</h2>
        <Button
          type="primary"
          onClick={() => {
            void cacheQuery.refetch();
            void runtimeQuery.refetch();
          }}
        >
          刷新
        </Button>
      </div>
      <div className="sm-cache-status-grid">
        <Card title="Redis 实时状态" loading={runtimeQuery.isLoading}>
          <Descriptions
            column={2}
            items={[
              { key: 'version', label: '版本', children: runtimeQuery.data?.version ?? '-' },
              { key: 'database', label: '数据库', children: runtimeQuery.data?.database ?? '-' },
              {
                key: 'clients',
                label: '客户端',
                children: runtimeQuery.data?.connectedClients ?? '-',
              },
              { key: 'keys', label: 'Key 数', children: runtimeQuery.data?.dbSize ?? '-' },
              { key: 'hits', label: '命中', children: runtimeQuery.data?.keyspaceHits ?? '-' },
              {
                key: 'misses',
                label: '未命中',
                children: runtimeQuery.data?.keyspaceMisses ?? '-',
              },
            ]}
          />
          <Statistic title="已使用内存" value={runtimeQuery.data?.usedMemoryDisplay ?? '-'} />
        </Card>
        <Card title="Redis 内存快照" loading={runtimeQuery.isLoading}>
          <SmChart option={memoryOption} ariaLabel="Redis 当前内存使用情况" />
        </Card>
      </div>
      <Card title="JetCache 实时统计" loading={cacheQuery.isLoading}>
        <Alert
          type="info"
          showIcon
          title="统计值来自 JetCache 当前采样周期；应用重启或定时统计输出后会重新开始累计。"
        />
        <Table
          rowKey="name"
          columns={columns}
          dataSource={cacheQuery.data?.caches ?? []}
          pagination={false}
        />
      </Card>
    </div>
  );
}
