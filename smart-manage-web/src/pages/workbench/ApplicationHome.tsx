import { Alert, Card, Empty, Tag } from 'antd';
import type { EChartsCoreOption } from 'echarts/core';
import { lazy, Suspense } from 'react';
import SchedulerHome from '@/domain/sys/scheduler/home/SchedulerHome';
import './ApplicationHome.css';

const SmChart = lazy(() => import('@/domain/common/chart/SmChart'));

interface ApplicationHomeProps {
  appNumber: string;
  appName: string;
}

interface DemoHomeProps {
  title: string;
  description: string;
  chartTitle: string;
  chartOption: EChartsCoreOption;
  capabilities: Array<{ title: string; description: string }>;
}

const HomeHeader = ({ title, description }: { title: string; description: string }) => (
  <header className="sm-app-home-header">
    <div>
      <h1>{title}</h1>
      <p>{description}</p>
    </div>
  </header>
);

const demoPieOption = (data: Array<{ name: string; value: number }>): EChartsCoreOption => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '演示分布',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '43%'],
      label: { formatter: '{b}\n{d}%' },
      data,
    },
  ],
});

const DemoHome = ({ title, description, chartTitle, chartOption, capabilities }: DemoHomeProps) => (
  <div className="sm-app-home">
    <HomeHeader title={title} description={description} />
    <Alert
      type="info"
      showIcon
      title="演示数据"
      description="当前应用尚未接入首页统计接口，下方图表只用于展示未来首页形态，不代表真实业务或运行状态。"
    />
    <section className="sm-app-home-columns">
      <Card
        className="sm-app-home-card sm-app-home-demo-chart"
        title={chartTitle}
        extra={<Tag color="blue">演示数据</Tag>}
      >
        <Suspense fallback={<div className="sm-app-home-chart-loading">图表加载中</div>}>
          <SmChart option={chartOption} ariaLabel={`${chartTitle}演示图表`} />
        </Suspense>
      </Card>
      <Card className="sm-app-home-card" title="实时数据">
        <Empty description="尚未接入统计接口" />
      </Card>
    </section>
    <section className="sm-app-home-capabilities">
      {capabilities.map((capability) => (
        <Card key={capability.title} className="sm-app-home-card">
          <strong>{capability.title}</strong>
          <p>{capability.description}</p>
        </Card>
      ))}
    </section>
  </div>
);

const ProcurementHome = () => (
  <DemoHome
    title="采购管理首页"
    description="采购业务入口与统计展示"
    chartTitle="采购申请状态构成"
    chartOption={demoPieOption([
      { name: '暂存', value: 18 },
      { name: '已提交', value: 32 },
      { name: '已审核', value: 43 },
      { name: '已关闭', value: 7 },
    ])}
    capabilities={[
      { title: '采购申请', description: '维护采购需求、明细与业务附件。' },
      { title: '状态流转', description: '区分暂存、提交和只读状态。' },
      { title: '统计待接入', description: '后续由真实聚合查询提供首页指标。' },
    ]}
  />
);

const SystemManagementHome = () => (
  <DemoHome
    title="系统管理首页"
    description="组织、权限、资料、平台结构与系统配置入口"
    chartTitle="系统管理构成"
    chartOption={demoPieOption([
      { name: '组织与用户', value: 25 },
      { name: '角色与权限', value: 20 },
      { name: '资料与编号', value: 20 },
      { name: '平台结构', value: 20 },
      { name: '系统配置', value: 15 },
    ])}
    capabilities={[
      { title: '组织与用户', description: '维护组织结构、用户和组织内角色关系。' },
      { title: '角色与权限', description: '通过权限目录配置功能访问能力。' },
      { title: '平台与配置', description: '维护应用结构、菜单和系统级配置。' },
    ]}
  />
);

const OperationsHome = () => (
  <DemoHome
    title="运维中心首页"
    description="运行监控、审计日志、诊断与高风险运维能力入口"
    chartTitle="运维能力构成"
    chartOption={demoPieOption([
      { name: '运行监控', value: 20 },
      { name: '审计日志', value: 20 },
      { name: '诊断分析', value: 20 },
      { name: '数据运维', value: 20 },
      { name: '脚本运维', value: 20 },
    ])}
    capabilities={[
      { title: '日志查询', description: '查看登录日志和操作日志。' },
      { title: '缓存诊断', description: '区分当前节点 LOCAL 与集群共享 Redis。' },
      { title: '高风险能力', description: 'SQL、脚本和诊断命令要求管理员身份。' },
    ]}
  />
);

/** 应用首页按应用编码显式分派，避免平台首页或其他应用首页被错误复用。 */
const ApplicationHome = ({ appNumber, appName }: ApplicationHomeProps) => {
  if (appNumber === 'procurement') return <ProcurementHome />;
  if (appNumber === 'monitor') return <OperationsHome />;
  if (appNumber === 'base') return <SystemManagementHome />;
  if (appNumber === 'scheduler') return <SchedulerHome />;
  return (
    <div className="sm-app-home">
      <HomeHeader title={`${appName}首页`} description="当前应用暂未配置专属首页" />
      <Card className="sm-app-home-card">
        <Empty description="暂无首页内容" />
      </Card>
    </div>
  );
};

export default ApplicationHome;
