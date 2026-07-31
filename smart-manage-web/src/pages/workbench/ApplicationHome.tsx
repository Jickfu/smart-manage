import { Card, Progress, Table, Tag } from 'antd';
import {
  AppstoreOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloudServerOutlined,
  DatabaseOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import './ApplicationHome.css';
import SchedulerHome from '@/domain/sys/scheduler/home/SchedulerHome';

interface ApplicationHomeProps {
  appNumber: string;
  appName: string;
}

const HomeHeader = ({ title, description }: { title: string; description: string }) => (
  <header className="sm-app-home-header">
    <div>
      <h1>{title}</h1>
      <p>{description}</p>
    </div>
    <span>数据更新时间：14:30</span>
  </header>
);

const ProcurementHome = () => (
  <div className="sm-app-home">
    <HomeHeader title="采购管理首页" description="聚焦采购申请、执行进度与金额分析" />
    <section className="sm-app-home-metrics">
      {[
        ['本月申请', '286', '较上月 +8.6%'],
        ['待我处理', '12', '3 项即将超时'],
        ['采购金额', '¥ 268.4万', '预算占用 61.2%'],
        ['按时完成率', '94.8%', '目标 95%'],
      ].map(([label, value, note]) => (
        <Card key={label} className="sm-app-home-card sm-app-home-metric">
          <span>{label}</span>
          <strong>{value}</strong>
          <small>{note}</small>
        </Card>
      ))}
    </section>
    <section className="sm-app-home-columns">
      <Card className="sm-app-home-card" title="采购执行阶段">
        <div className="sm-purchase-flow">
          {[
            ['申请中', '48'],
            ['审批中', '27'],
            ['采购执行', '19'],
            ['已完成', '175'],
          ].map(([label, value], index) => (
            <div key={label}>
              <span>{index + 1}</span>
              <strong>{value}</strong>
              <small>{label}</small>
            </div>
          ))}
        </div>
      </Card>
      <Card className="sm-app-home-card" title="预算执行">
        <div className="sm-app-home-progress">
          <strong>61.2%</strong>
          <Progress percent={61.2} showInfo={false} />
          <span>已执行 1,842 万 / 总预算 3,010 万</span>
        </div>
      </Card>
    </section>
    <Card className="sm-app-home-card" title="待处理采购申请">
      <Table
        size="small"
        pagination={false}
        dataSource={[
          { key: '1', number: 'PR20260723001', subject: '研发中心办公设备采购', status: '待审批' },
          { key: '2', number: 'PR20260722008', subject: '华南区域市场活动物料', status: '待提交' },
          { key: '3', number: 'PR20260721016', subject: '数据中心备件采购', status: '待审批' },
        ]}
        columns={[
          { title: '单据编号', dataIndex: 'number', width: 160 },
          { title: '采购事项', dataIndex: 'subject' },
          {
            title: '状态',
            dataIndex: 'status',
            width: 100,
            render: (status: string) => (
              <Tag color={status === '待提交' ? 'warning' : 'processing'}>{status}</Tag>
            ),
          },
        ]}
      />
    </Card>
  </div>
);

const ModelingHome = () => (
  <div className="sm-app-home">
    <HomeHeader title="系统建模首页" description="维护基础资料、应用能力与权限模型" />
    <section className="sm-model-home-summary">
      <Card className="sm-app-home-card sm-model-home-health">
        <SafetyCertificateOutlined />
        <div>
          <strong>基础模型运行正常</strong>
          <span>今日配置变更 6 项，均已生效</span>
        </div>
      </Card>
      <Card className="sm-app-home-card sm-model-home-score">
        <Progress type="circle" percent={92} size={88} />
        <div>
          <strong>配置完整度</strong>
          <span>1 个角色待复核权限</span>
        </div>
      </Card>
    </section>
    <section className="sm-model-home-entries">
      {[
        [<TeamOutlined key="user" />, '用户与角色', '1,286 个用户 / 42 个角色'],
        [<AppstoreOutlined key="app" />, '应用与菜单', '18 个应用 / 326 个菜单'],
        [<SettingOutlined key="view" />, '界面配置', '登录页与系统外观'],
      ].map(([icon, title, description]) => (
        <Card key={String(title)} className="sm-app-home-card sm-model-home-entry">
          <span>{icon}</span>
          <div>
            <strong>{title}</strong>
            <small>{description}</small>
          </div>
        </Card>
      ))}
    </section>
    <Card className="sm-app-home-card" title="最近配置变更">
      <ul className="sm-app-home-events">
        <li>
          <CheckCircleOutlined />
          <span>采购管理应用菜单配置已更新</span>
          <time>10 分钟前</time>
        </li>
        <li>
          <TeamOutlined />
          <span>管理员角色新增 2 项页面权限</span>
          <time>1 小时前</time>
        </li>
        <li>
          <AppstoreOutlined />
          <span>系统监控应用说明已修改</span>
          <time>昨天</time>
        </li>
      </ul>
    </Card>
  </div>
);

const MonitorHome = () => (
  <div className="sm-app-home">
    <HomeHeader title="系统监控首页" description="实时掌握服务、数据源、任务与缓存状态" />
    <section className="sm-monitor-home-health">
      <div>
        <CheckCircleOutlined />
        <span>
          <strong>系统运行正常</strong>
          <small>所有核心服务均可用</small>
        </span>
      </div>
      <time>已连续稳定运行 28 天 16 小时</time>
    </section>
    <section className="sm-app-home-metrics">
      {[
        [<CloudServerOutlined key="node" />, '服务节点', '4 / 4'],
        [<DatabaseOutlined key="db" />, '数据源', '3 / 3'],
        [<ClockCircleOutlined key="job" />, '定时任务', '36'],
        [<WarningOutlined key="alert" />, '待处理告警', '2'],
      ].map(([icon, label, value]) => (
        <Card key={String(label)} className="sm-app-home-card sm-monitor-home-metric">
          <span>{icon}</span>
          <div>
            <small>{label}</small>
            <strong>{value}</strong>
          </div>
        </Card>
      ))}
    </section>
    <section className="sm-app-home-columns">
      <Card className="sm-app-home-card" title="资源使用率">
        <div className="sm-monitor-home-resources">
          {(
            [
              ['CPU', 28],
              ['内存', 54],
              ['JVM 堆', 46],
              ['连接池', 32],
            ] as const
          ).map(([label, value]) => (
            <div key={label}>
              <span>{label}</span>
              <Progress percent={value} size="small" />
            </div>
          ))}
        </div>
      </Card>
      <Card className="sm-app-home-card" title="实时事件">
        <ul className="sm-app-home-events">
          <li>
            <CheckCircleOutlined />
            <span>采购任务执行成功</span>
            <time>14:28</time>
          </li>
          <li>
            <WarningOutlined />
            <span>缓存命中率低于阈值</span>
            <time>14:16</time>
          </li>
          <li>
            <DatabaseOutlined />
            <span>数据库连接池自动扩容</span>
            <time>13:52</time>
          </li>
        </ul>
      </Card>
    </section>
  </div>
);

/** 应用首页按应用编码显式分派，避免平台首页或其他应用首页被错误复用。 */
const ApplicationHome = ({ appNumber, appName }: ApplicationHomeProps) => {
  if (appNumber === 'procurement') return <ProcurementHome />;
  if (appNumber === 'monitor') return <MonitorHome />;
  if (appNumber === 'base') return <ModelingHome />;
  if (appNumber === 'scheduler') return <SchedulerHome />;
  return (
    <div className="sm-app-home">
      <HomeHeader title={`${appName}首页`} description="当前应用暂未配置专属首页" />
    </div>
  );
};

export default ApplicationHome;
