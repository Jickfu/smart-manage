import { Card, Result, Statistic, Table, Tag } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { monitorOverviewApi } from './api';
import './monitorHome.css';
const healthColor = (status: string) =>
  status === 'UP' ? 'success' : status === 'UNKNOWN' ? 'default' : 'error';
const MonitorHome = () => {
  const query = useQuery({
    queryKey: ['sys-monitor-overview'],
    queryFn: monitorOverviewApi,
    refetchInterval: 10000,
  });
  if (query.error)
    return <Result status="error" title="运维总览加载失败" subTitle={query.error.message} />;
  const data = query.data;
  return (
    <div className="sm-app-home sm-monitor-home">
      <header className="sm-app-home-header">
        <div>
          <h1>运维中心</h1>
          <p>运行健康、当前异常与拓扑关注</p>
        </div>
      </header>
      <div className="sm-monitor-home-metrics">
        <Card loading={query.isLoading}>
          <Statistic
            title="主机遥测可用 / 总数"
            value={`${data?.hostTelemetryAvailable ?? 0} / ${data?.hostTotal ?? 0}`}
          />
        </Card>
        <Card loading={query.isLoading}>
          <Statistic
            title="应用在线 / 总数"
            value={`${data?.applicationOnline ?? 0} / ${data?.applicationTotal ?? 0}`}
          />
        </Card>
        <Card loading={query.isLoading}>
          <Statistic
            title="PENDING / FIRING / CRITICAL"
            value={`${data?.pendingCount ?? 0} / ${data?.firingCount ?? 0} / ${data?.criticalCount ?? 0}`}
          />
        </Card>
        <Card loading={query.isLoading} title="基础设施">
          <Tag color={healthColor(data?.databaseHealth ?? 'UNKNOWN')}>
            DB {data?.databaseHealth ?? 'UNKNOWN'}
          </Tag>
          <Tag color={healthColor(data?.redisHealth ?? 'UNKNOWN')}>
            Redis {data?.redisHealth ?? 'UNKNOWN'}
          </Tag>
        </Card>
      </div>
      <Card title="当前异常">
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
      <Card title="拓扑摘要">
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
