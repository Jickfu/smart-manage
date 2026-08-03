import { Card, Result, Statistic, Tag } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { schedulerHomeApi } from './api';
import './SchedulerHome.css';

const SchedulerHome = () => {
  const summaryQuery = useQuery({
    queryKey: ['sys', 'scheduler', 'home', 'summary'],
    queryFn: schedulerHomeApi.summary,
  });
  const summary = summaryQuery.data;
  const maximum = Math.max(
    1,
    ...(summary?.trends.map((item) => item.success + item.failed + item.skipped) ?? []),
  );
  const statistics = [
    ['任务总数', summary?.totalJobs ?? 0],
    ['已启用', summary?.enabledJobs ?? 0],
    ['已暂停', summary?.pausedJobs ?? 0],
    ['正在执行', summary?.runningExecutions ?? 0],
    ['今日执行', summary?.todayExecutions ?? 0],
    ['今日失败', summary?.todayFailedExecutions ?? 0],
  ] as const;

  if (summaryQuery.error) {
    return (
      <div className="sm-app-home">
        <header className="sm-app-home-header">
          <div>
            <h1>任务调度首页</h1>
            <p>掌握任务状态与执行趋势</p>
          </div>
        </header>
        <Card className="sm-app-home-card">
          <Result status="error" title="调度统计加载失败" subTitle={summaryQuery.error.message} />
        </Card>
      </div>
    );
  }

  return (
    <div className="sm-app-home">
      <header className="sm-app-home-header">
        <div>
          <h1>任务调度首页</h1>
          <p>掌握任务状态与近七日执行趋势</p>
        </div>
      </header>
      <section className="sm-scheduler-home-statistics">
        {statistics.map(([title, value]) => (
          <Card
            key={title}
            className="sm-app-home-card"
            size="small"
            loading={summaryQuery.isLoading}
          >
            <Statistic title={title} value={value} />
          </Card>
        ))}
      </section>
      <Card
        className="sm-app-home-card"
        title="近 7 天执行趋势"
        loading={summaryQuery.isLoading}
        extra={
          <span className="sm-scheduler-home-legend">
            <Tag color="success">成功</Tag>
            <Tag color="error">失败</Tag>
            <Tag>互斥跳过</Tag>
          </span>
        }
      >
        <div className="sm-scheduler-home-chart">
          {summary?.trends.map((item) => (
            <div
              key={item.date}
              className="sm-scheduler-home-chart-column"
              title={`${item.date}：成功 ${item.success}，失败 ${item.failed}，跳过 ${item.skipped}`}
            >
              <svg viewBox="0 0 42 160" aria-hidden="true">
                <rect
                  className="sm-scheduler-home-bar--success"
                  x="2"
                  y={158 - (item.success / maximum) * 150}
                  width="11"
                  height={Math.max(3, (item.success / maximum) * 150)}
                />
                <rect
                  className="sm-scheduler-home-bar--failed"
                  x="16"
                  y={158 - (item.failed / maximum) * 150}
                  width="11"
                  height={Math.max(3, (item.failed / maximum) * 150)}
                />
                <rect
                  className="sm-scheduler-home-bar--skipped"
                  x="30"
                  y={158 - (item.skipped / maximum) * 150}
                  width="11"
                  height={Math.max(3, (item.skipped / maximum) * 150)}
                />
              </svg>
              <strong>{item.success + item.failed + item.skipped}</strong>
              <span>{item.date.slice(5)}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
};

export default SchedulerHome;
