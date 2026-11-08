import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { useMemo, useState } from 'react';
import { Button, Card, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import AppModal from '@/domain/common/component/AppModal';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { useQuery } from '@tanstack/react-query';
import type { EChartsCoreOption } from 'echarts/core';
import { schedulerHomeApi } from './api';
import type { SchedulerTrend } from './types';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import SmChart from '@/domain/common/chart/SmChart';
import { chartTaskStatusColors } from '@/domain/common/chart/chartColors';
import './SchedulerHome.css';

const trendColumns: ColumnsType<SchedulerTrend> = [
  { title: '日期', dataIndex: 'date', ellipsis: true, width: 160 },
  // 次数是核对数据，不省略或缩写原值。
  { title: '成功（次）', dataIndex: 'success', ellipsis: false },
  // 次数是核对数据，不省略或缩写原值。
  { title: '失败（次）', dataIndex: 'failed', ellipsis: false },
  // 次数是核对数据，不省略或缩写原值。
  { title: '互斥跳过（次）', dataIndex: 'skipped', ellipsis: false },
];

const SchedulerHome = () => {
  const [dataOpen, setDataOpen] = useState(false);
  const summaryQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: ['sys', 'scheduler', 'home', 'summary'],
    queryFn: schedulerHomeApi.summary,
  });
  const summary = summaryQuery.data;
  const trendOption = useMemo<EChartsCoreOption>(
    () => ({
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0, data: ['成功', '失败', '互斥跳过'] },
      grid: { left: 42, right: 18, top: 18, bottom: 42 },
      xAxis: {
        type: 'category',
        data: summary?.trends.map((item) => item.date.slice(5)) ?? [],
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '成功',
          itemStyle: { color: chartTaskStatusColors.success },
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.success) ?? [],
        },
        {
          name: '失败',
          itemStyle: { color: chartTaskStatusColors.failed },
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.failed) ?? [],
        },
        {
          name: '互斥跳过',
          itemStyle: { color: chartTaskStatusColors.skipped },
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.skipped) ?? [],
        },
      ],
    }),
    [summary?.trends],
  );

  if (getBlockingQueryError(summaryQuery)) {
    return (
      <div className="sm-app-home">
        <QuickLaunchCard scope="APPLICATION" appNumber="scheduler" />
        <Card className="sm-app-home-card">
          <RequestErrorState
            title="调度统计加载失败"
            error={summaryQuery.error}
            onRetry={() => void summaryQuery.refetch()}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="sm-app-home">
      <QuickLaunchCard scope="APPLICATION" appNumber="scheduler" />
      <HomeCardGrid>
        <Card
          className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-2"
          loading={summaryQuery.isLoading}
        >
          <div className="sm-app-home-metric-title">任务总数</div>
          <div className="sm-app-home-metric-value">{summary?.totalJobs ?? 0}</div>
          <div className="sm-app-home-metric-caption">
            已启用 {summary?.enabledJobs ?? 0} · 已暂停 {summary?.pausedJobs ?? 0}
          </div>
        </Card>
        <Card
          className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-2"
          loading={summaryQuery.isLoading}
        >
          <div className="sm-app-home-metric-title">今日执行</div>
          <div className="sm-app-home-metric-value">{summary?.todayExecutions ?? 0}</div>
          <div className="sm-app-home-metric-caption">
            失败{' '}
            <span className="sm-scheduler-home-failed">{summary?.todayFailedExecutions ?? 0}</span>
          </div>
        </Card>
      </HomeCardGrid>
      <HomeCardGrid>
        <Card
          className="sm-app-home-card sm-home-card-span-3"
          title="近 7 天执行趋势"
          extra={
            <Button type="link" disabled={!summary} onClick={() => setDataOpen(true)}>
              查看数据
            </Button>
          }
          loading={summaryQuery.isLoading}
        >
          <div className="sm-scheduler-home-chart">
            <SmChart option={trendOption} ariaLabel="近七天任务执行趋势" />
          </div>
        </Card>
        <Card
          className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-1"
          loading={summaryQuery.isLoading}
        >
          <div className="sm-app-home-metric-title">正在执行</div>
          <div className="sm-app-home-metric-value">{summary?.runningExecutions ?? 0}</div>
          <div className="sm-app-home-metric-caption">当前处于运行态的执行实例</div>
        </Card>
      </HomeCardGrid>
      <AppModal
        title="近 7 天执行趋势数据"
        open={dataOpen}
        width={800}
        classNames={{ body: 'sm-scheduler-trend-data-body' }}
        onCancel={() => setDataOpen(false)}
        footer={<Button onClick={() => setDataOpen(false)}>关闭</Button>}
      >
        <p>按日期展示当前图表使用的全部统计数据，单位为次。图例显隐不改变本表口径。</p>
        {summaryQuery.error && <p>刷新失败，当前展示上次成功加载的数据。</p>}
        <Table<SchedulerTrend>
          size="small"
          rowKey="date"
          pagination={false}
          columns={trendColumns}
          dataSource={summary?.trends ?? []}
          scroll={{ x: 640 }}
        />
      </AppModal>
    </div>
  );
};

export default SchedulerHome;
