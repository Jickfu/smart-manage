import { useState } from 'react';
import { Alert, Button, Card, Progress, Space, Statistic, Switch, Table, Tag, Tooltip } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { cacheApi } from './api';
import { cacheQueryKeys } from './queryKeys';
import type { ManagedCache } from './types';
import './cachePage.css';

function formatBytes(bytes: number) {
  if (bytes <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const unitIndex = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  return `${(bytes / 1024 ** unitIndex).toFixed(unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`;
}

function formatRate(rate?: number) {
  return rate === undefined ? '-' : `${(rate * 100).toFixed(1)}%`;
}

const cacheStates = {
  NOT_CREATED: { color: 'default', label: '尚未创建' },
  UNAVAILABLE: { color: 'warning', label: '统计不可用' },
  IDLE: { color: 'processing', label: '本周期无访问' },
  ACTIVE: { color: 'success', label: '本周期有访问' },
} as const;

export default function CachePage(_: PageComponentProps) {
  const [autoRefresh, setAutoRefresh] = useState(true);
  const refetchInterval = autoRefresh ? 10_000 : false;
  const cacheQuery = useQuery({
    queryKey: cacheQueryKeys.overview(),
    queryFn: cacheApi.overview,
    refetchInterval,
  });
  const runtimeQuery = useQuery({
    queryKey: cacheQueryKeys.runtime(),
    queryFn: cacheApi.runtime,
    refetchInterval,
  });
  const caches = cacheQuery.data?.caches ?? [];
  const totalReads = caches.reduce((total, cache) => total + cache.getCount, 0);
  const totalHits = caches.reduce((total, cache) => total + cache.hitCount, 0);
  const totalMisses = caches.reduce((total, cache) => total + cache.missCount, 0);
  const jetCacheHitRate = totalReads > 0 ? totalHits / totalReads : undefined;
  const activeCount = caches.filter((cache) => cache.state === 'ACTIVE').length;
  const createdCount = caches.filter((cache) => cache.state !== 'NOT_CREATED').length;
  const maximumMemory = runtimeQuery.data?.maxMemoryBytes ?? 0;
  const usedMemory = runtimeQuery.data?.usedMemoryBytes ?? 0;
  const memoryPercentage =
    maximumMemory > 0 ? Math.min((usedMemory / maximumMemory) * 100, 100) : undefined;
  const statStartedAt = caches.find((cache) => cache.statStartedAt)?.statStartedAt;

  const columns: ColumnsType<ManagedCache> = [
    {
      title: '已登记缓存',
      dataIndex: 'displayName',
      minWidth: 210,
      render: (value, record) => (
        <div className="sm-cache-name-cell">
          <strong>{value}</strong>
          <span>{record.description}</span>
          <code>{record.name}</code>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'state',
      width: 124,
      render: (value: ManagedCache['state']) => {
        const state = cacheStates[value];
        return <Tag color={state.color}>{state.label}</Tag>;
      },
    },
    {
      title: '策略',
      width: 120,
      render: (_, record) => (
        <div className="sm-cache-compact-cell">
          <span>{record.type}</span>
          <small>TTL {record.expireSeconds} 秒</small>
        </div>
      ),
    },
    {
      title: '本周期读取',
      dataIndex: 'getCount',
      width: 150,
      render: (value: number, record) => (
        <div className="sm-cache-compact-cell">
          <strong>{value}</strong>
          <small>
            命中 {record.hitCount} · 未命中 {record.missCount}
          </small>
        </div>
      ),
    },
    {
      title: '命中率',
      dataIndex: 'hitRate',
      width: 150,
      render: (value: number, record) =>
        record.getCount > 0 ? (
          <Progress percent={value * 100} size="small" format={() => formatRate(value)} />
        ) : (
          <span className="sm-cache-muted">-</span>
        ),
    },
    {
      title: '性能',
      width: 140,
      render: (_, record) => (
        <div className="sm-cache-compact-cell">
          <span>{record.qps.toFixed(2)} QPS</span>
          <small>{record.averageGetTime.toFixed(2)} ms 平均</small>
        </div>
      ),
    },
    {
      title: '失败',
      dataIndex: 'failCount',
      width: 72,
      render: (value: number) => (
        <span className={value > 0 ? 'sm-cache-danger' : undefined}>{value}</span>
      ),
    },
  ];

  const error = cacheQuery.error ?? runtimeQuery.error;
  return (
    <EditPageShell
      title="缓存状态"
      loading={false}
      actions={
        <Space>
          <span>每 10 秒刷新</span>
          <Switch checked={autoRefresh} onChange={setAutoRefresh} />
          <Button
            type="primary"
            loading={cacheQuery.isFetching || runtimeQuery.isFetching}
            onClick={() => {
              void cacheQuery.refetch();
              void runtimeQuery.refetch();
            }}
          >
            刷新
          </Button>
        </Space>
      }
    >
      <div className="sm-cache-page">
        {error && <Alert type="error" showIcon title={error.message} />}
        <Card
          size="small"
          className="sm-cache-overview"
          loading={runtimeQuery.isLoading || cacheQuery.isLoading}
        >
          <section className="sm-cache-overview-section">
            <div className="sm-cache-section-heading">
              <strong>Redis</strong>
              <Tag color={runtimeQuery.data?.available ? 'success' : 'error'}>
                {runtimeQuery.data?.available ? '运行正常' : '不可用'}
              </Tag>
              <span>
                DB {runtimeQuery.data?.database ?? '-'} · Redis {runtimeQuery.data?.version ?? '-'}
              </span>
            </div>
            <div className="sm-cache-kpis">
              <Statistic title="Key 数" value={runtimeQuery.data?.dbSize ?? '-'} />
              <Statistic title="客户端" value={runtimeQuery.data?.connectedClients ?? '-'} />
              <Statistic title="累计命中率" value={formatRate(runtimeQuery.data?.hitRate)} />
            </div>
            <div className="sm-cache-memory-line">
              <span>内存</span>
              <Progress
                percent={memoryPercentage ?? 0}
                showInfo={memoryPercentage !== undefined}
                size="small"
                format={() =>
                  `${runtimeQuery.data?.usedMemoryDisplay ?? '-'} / ${formatBytes(maximumMemory)}`
                }
              />
              {memoryPercentage === undefined && (
                <small>{runtimeQuery.data?.usedMemoryDisplay ?? '-'} · 未设置 maxmemory</small>
              )}
            </div>
          </section>
          <section className="sm-cache-overview-section">
            <div className="sm-cache-section-heading">
              <strong>JetCache 当前周期</strong>
              <Tag>{cacheQuery.data?.instanceId ?? '-'}</Tag>
              <Tooltip title="以下缓存来自后端统一缓存目录，不是根据当前 Redis Key 临时推断。未创建的登记缓存也会保留显示。">
                <span className="sm-cache-help">已登记 {caches.length} 项</span>
              </Tooltip>
            </div>
            <div className="sm-cache-kpis sm-cache-kpis-four">
              <Statistic title="读取" value={totalReads} />
              <Statistic title="命中率" value={formatRate(jetCacheHitRate)} />
              <Statistic title="活跃" value={activeCount} suffix={`/ ${createdCount}`} />
              <Statistic title="未命中" value={totalMisses} />
            </div>
            <div className="sm-cache-period-note">
              {statStartedAt
                ? `周期开始：${statStartedAt}`
                : '业务尚未使用缓存时，不会为了监控而创建实例。'}
              <span>统计每 15 分钟输出并重置，仅代表当前节点。</span>
            </div>
          </section>
        </Card>
        <Card
          size="small"
          className="sm-cache-table-card"
          title="已登记 JetCache"
          extra={<span className="sm-cache-muted">目录驱动 · 当前节点本周期</span>}
          loading={cacheQuery.isLoading}
        >
          <Table
            rowKey="name"
            columns={columns}
            dataSource={caches}
            pagination={false}
            size="small"
            scroll={{ x: 1000 }}
          />
        </Card>
      </div>
    </EditPageShell>
  );
}
