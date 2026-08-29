import { memo, useMemo } from 'react';
import { Card, Tag } from 'antd';
import type { EChartsCoreOption } from 'echarts/core';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import SmChart from '@/domain/common/chart/SmChart';
import './Home.css';

const exampleTag = <Tag color="blue">示例</Tag>;

const Home = () => {
  const chartOptions = useMemo<Record<'trend' | 'achievement' | 'execution', EChartsCoreOption>>(
    () => ({
      execution: {
        tooltip: { trigger: 'item' },
        legend: { bottom: 0 },
        series: [
          {
            type: 'pie',
            radius: ['48%', '70%'],
            center: ['50%', '44%'],
            label: { show: false },
            data: [
              { value: 38, name: '待处理' },
              { value: 24, name: '处理中' },
              { value: 62, name: '已完成' },
            ],
          },
        ],
      },
      trend: {
        tooltip: { trigger: 'axis' },
        grid: { left: 38, right: 18, top: 20, bottom: 32 },
        xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
        yAxis: { type: 'value' },
        series: [
          {
            type: 'line',
            smooth: true,
            data: [12, 18, 15, 26, 22, 34],
            areaStyle: { opacity: 0.18 },
          },
        ],
      },
      achievement: {
        series: [
          {
            type: 'gauge',
            startAngle: 210,
            endAngle: -30,
            progress: { show: true, width: 12 },
            axisLine: { lineStyle: { width: 12 } },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { show: false },
            pointer: { show: false },
            detail: { valueAnimation: true, formatter: '{value}%', fontSize: 30 },
            data: [{ value: 76 }],
          },
        ],
      },
    }),
    [],
  );

  return (
    <div className="sm-system-home">
      <QuickLaunchCard scope="SYSTEM" />
      <HomeCardGrid className="sm-system-home-charts">
        <Card className="sm-home-card" title="费用执行" extra={exampleTag} variant="borderless">
          <div className="sm-system-home-number-example">
            <strong>26</strong>
            <span>待审批</span>
          </div>
        </Card>
        <Card className="sm-home-card" title="业务趋势" extra={exampleTag} variant="borderless">
          <SmChart option={chartOptions.trend} ariaLabel="示例业务趋势图" />
        </Card>
        <Card className="sm-home-card" title="目标达成率" extra={exampleTag} variant="borderless">
          <SmChart option={chartOptions.achievement} ariaLabel="示例目标达成率仪表盘" />
        </Card>
        <Card className="sm-home-card" title="事项执行" extra={exampleTag} variant="borderless">
          <SmChart option={chartOptions.execution} ariaLabel="示例事项执行分布图" />
        </Card>
      </HomeCardGrid>
    </div>
  );
};

export default memo(Home);
