import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { Card, Table, Tag } from 'antd';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { useQuery } from '@tanstack/react-query';
import { monitorOverviewApi } from './api';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import './monitorHome.css';
const healthColor = (status: string) =>
  status === 'UP' ? 'success' : status === 'UNKNOWN' ? 'default' : 'error';
const MonitorHome = () => {
  const query = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: ['sys-monitor-overview'],
    queryFn: monitorOverviewApi,
    refetchInterval: 10000,
  });
  if (getBlockingQueryError(query))
    return (
      <div className="sm-app-home sm-monitor-home">
        <QuickLaunchCard scope="APPLICATION" appNumber="monitor" />
        <RequestErrorState
          title="运维总览加载失败"
          error={query.error}
          onRetry={() => void query.refetch()}
        />
      </div>
    );
  const data = query.data;
  return (
    <div className="sm-app-home sm-monitor-home">
      <QuickLaunchCard scope="APPLICATION" appNumber="monitor" />
      <HomeCardGrid className="sm-monitor-home-metrics">
        <Card className="sm-app-home-card" loading={query.isLoading}>
          <div className="sm-app-home-metric-title">主机遥测</div>
          <div className="sm-monitor-home-ratio">
            <strong>{data?.hostTelemetryAvailable ?? 0}</strong>
            <span>/ {data?.hostTotal ?? 0}</span>
          </div>
          <div className="sm-app-home-metric-caption">可用 / 总数</div>
        </Card>
        <Card className="sm-app-home-card" loading={query.isLoading}>
          <div className="sm-app-home-metric-title">应用实例</div>
          <div className="sm-monitor-home-ratio">
            <strong>{data?.applicationOnline ?? 0}</strong>
            <span>/ {data?.applicationTotal ?? 0}</span>
          </div>
          <div className="sm-app-home-metric-caption">在线 / 总数</div>
        </Card>
        <Card className="sm-app-home-card" loading={query.isLoading}>
          <div className="sm-app-home-metric-title">告警状态</div>
          <div className="sm-monitor-home-alerts">
            <div>
              <strong>{data?.pendingCount ?? 0}</strong>
              <span>PENDING</span>
            </div>
            <div>
              <strong>{data?.firingCount ?? 0}</strong>
              <span>FIRING</span>
            </div>
            <div className="sm-monitor-home-alert--critical">
              <strong>{data?.criticalCount ?? 0}</strong>
              <span>CRITICAL</span>
            </div>
          </div>
        </Card>
        <Card className="sm-app-home-card" loading={query.isLoading}>
          <div className="sm-app-home-metric-title">基础设施</div>
          <div className="sm-monitor-home-infrastructure">
            <Tag color={healthColor(data?.databaseHealth ?? 'UNKNOWN')}>
              DB {data?.databaseHealth ?? 'UNKNOWN'}
            </Tag>
            <Tag color={healthColor(data?.redisHealth ?? 'UNKNOWN')}>
              Redis {data?.redisHealth ?? 'UNKNOWN'}
            </Tag>
          </div>
        </Card>
      </HomeCardGrid>
      <Card className="sm-app-home-card" title="当前异常">
        <Table
          size="small"
          pagination={false}
          rowKey={(item) => `${item.ruleCode}:${item.scopeType}:${item.scopeId}`}
          dataSource={data?.currentAbnormal ?? []}
          columns={[
            {
              title: '级别',
              dataIndex: 'severity',
              width: 100,
              render: (value: string) => (
                <Tag color={value === 'CRITICAL' ? 'error' : 'warning'}>{value}</Tag>
              ),
            },
            { title: '规则', dataIndex: 'ruleCode', width: 220 },
            {
              title: '对象',
              render: (_, item) => `${item.scopeType} · ${item.scopeId}`,
              width: 240,
            },
            { title: '摘要', dataIndex: 'summary' },
          ]}
        />
      </Card>
      <Card className="sm-app-home-card" title="拓扑摘要">
        <Table
          size="small"
          pagination={false}
          rowKey="hostId"
          dataSource={data?.topology ?? []}
          columns={[
            { title: '主机', dataIndex: 'hostName' },
            { title: 'Host ID', dataIndex: 'hostId', width: 220 },
            {
              title: '遥测',
              dataIndex: 'telemetryStatus',
              width: 160,
              render: (value: string) => (
                <Tag color={value === 'UP' ? 'success' : 'warning'}>
                  {value === 'UP' ? '可用' : '不可用'}
                </Tag>
              ),
            },
            {
              title: '实例在线 / 总数',
              width: 150,
              render: (_, item) => `${item.onlineInstances} / ${item.totalInstances}`,
            },
          ]}
        />
      </Card>
    </div>
  );
};

export default MonitorHome;
