import { useMemo } from 'react';
import { Card, Result } from 'antd';
import { useQuery } from '@tanstack/react-query';
import type { EChartsCoreOption } from 'echarts/core';
import { schedulerHomeApi } from './api';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import SmChart from '@/domain/common/chart/SmChart';
import './SchedulerHome.css';

const SchedulerHome = () => {
  const summaryQuery = useQuery({
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
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.success) ?? [],
        },
        {
          name: '失败',
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.failed) ?? [],
        },
        {
          name: '互斥跳过',
          type: 'bar',
          stack: 'execution',
          data: summary?.trends.map((item) => item.skipped) ?? [],
        },
      ],
    }),
    [summary?.trends],
  );

  if (summaryQuery.error) {
    return (
      <div className="sm-app-home">
        <QuickLaunchCard scope="APPLICATION" appNumber="scheduler" />
        <Card className="sm-app-home-card">
          <Result status="error" title="调度统计加载失败" subTitle={summaryQuery.error.message} />
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
    </div>
  );
};

export default SchedulerHome;
