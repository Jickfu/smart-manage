import { useMemo } from 'react';
import { Card, Tag } from 'antd';
import type { EChartsCoreOption } from 'echarts/core';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import SmChart from '@/domain/common/chart/SmChart';
import './MessageHome.css';

const exampleTag = <Tag color="blue">示例</Tag>;

const MessageHome = () => {
  const trendOption = useMemo<EChartsCoreOption>(
    () => ({
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0 },
      grid: { left: 42, right: 18, top: 18, bottom: 42 },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
      yAxis: { type: 'value' },
      series: [
        { name: '邮件', type: 'line', smooth: true, data: [82, 96, 74, 121, 108, 54, 67] },
        { name: '站内消息', type: 'line', smooth: true, data: [42, 61, 55, 78, 69, 31, 38] },
      ],
    }),
    [],
  );

  return (
    <div className="sm-app-home">
      <QuickLaunchCard scope="APPLICATION" appNumber="message" />
      <HomeCardGrid>
        {[
          ['今日投递', 128, '邮件与站内消息'],
          ['等待发送', 12, '处于待处理队列'],
        ].map(([title, value, caption]) => (
          <Card
            key={title}
            className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-2"
          >
            <div className="sm-app-home-metric-title">
              <span>{title}</span>
              {exampleTag}
            </div>
            <div className="sm-app-home-metric-value">{value}</div>
            <div className="sm-app-home-metric-caption">{caption}</div>
          </Card>
        ))}
      </HomeCardGrid>
      <HomeCardGrid>
        <Card className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-1">
          <div className="sm-app-home-metric-title">
            <span>失败待处理</span>
            {exampleTag}
          </div>
          <div className="sm-app-home-metric-value sm-app-home-metric-value--danger">3</div>
          <div className="sm-app-home-metric-caption">需要检查或重新发送</div>
        </Card>
        <Card
          className="sm-app-home-card sm-home-card-span-3"
          title="近 7 天投递趋势"
          extra={exampleTag}
        >
          <div className="sm-message-home-chart">
            <SmChart option={trendOption} ariaLabel="示例近七天消息投递趋势" />
          </div>
        </Card>
      </HomeCardGrid>
    </div>
  );
};

export default MessageHome;
