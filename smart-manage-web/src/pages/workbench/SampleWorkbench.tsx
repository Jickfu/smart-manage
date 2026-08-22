import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { Button, Card, Progress, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  AppstoreOutlined,
  AuditOutlined,
  BellOutlined,
  FileAddOutlined,
  FileSearchOutlined,
  SettingOutlined,
  ShoppingCartOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import './SampleWorkbench.css';

interface SampleWorkbenchProps {
  title?: string;
}

interface PendingItem {
  key: string;
  number: string;
  subject: string;
  applicant: string;
  status: string;
}

const pendingItems: PendingItem[] = [
  {
    key: '1',
    number: 'PR20260723001',
    subject: '研发中心办公设备采购',
    applicant: '李明',
    status: '待审批',
  },
  {
    key: '2',
    number: 'PR20260722008',
    subject: '华南区域市场活动物料',
    applicant: '周婷',
    status: '待补充',
  },
  {
    key: '3',
    number: 'PR20260721016',
    subject: '数据中心备件采购',
    applicant: '陈远',
    status: '待审批',
  },
];

const pendingColumns: ColumnsType<PendingItem> = [
  {
    title: '单据编号',
    dataIndex: 'number',
    width: 132,
    render: (number: string) => (
      <button type="button" className="sm-workbench-link">
        {number}
      </button>
    ),
  },
  { title: '采购事项', dataIndex: 'subject', ellipsis: true },
  { title: '申请人', dataIndex: 'applicant', width: 72 },
  {
    title: '状态',
    dataIndex: 'status',
    width: 78,
    render: (status: string) => (
      <Tag color={status === '待审批' ? 'processing' : 'warning'}>{status}</Tag>
    ),
  },
];

const quickActions = [
  { key: 'purchase', label: '采购申请', icon: <ShoppingCartOutlined /> },
  { key: 'approval', label: '待办审批', icon: <AuditOutlined /> },
  { key: 'user', label: '用户管理', icon: <TeamOutlined /> },
  { key: 'application', label: '应用管理', icon: <AppstoreOutlined /> },
  { key: 'document', label: '单据查询', icon: <FileSearchOutlined /> },
  { key: 'new', label: '新建事项', icon: <FileAddOutlined /> },
  { key: 'message', label: '消息中心', icon: <BellOutlined /> },
  { key: 'setting', label: '个人设置', icon: <SettingOutlined /> },
];

/** 工作台样例，集中覆盖快捷入口、关键数字、分析、列表和进度类卡片。 */
const SampleWorkbench = ({ title = '工作台' }: SampleWorkbenchProps) => {
  const feedback = useOperationFeedback();

  const showSampleNotice = () => {
    feedback.info('当前为工作台样例数据');
  };

  return (
    <div className="sm-sample-workbench">
      <section className="sm-workbench-operation">
        <div>
          <div className="sm-workbench-welcome">{title}</div>
          <div className="sm-workbench-date">2026年7月23日，欢迎回来</div>
        </div>
        <div className="sm-workbench-operation-actions">
          <span>当前组织：Smart Manage</span>
          <Button type="link" onClick={showSampleNotice}>
            切换方案
          </Button>
          <Button type="link" onClick={showSampleNotice}>
            调整布局
          </Button>
        </div>
      </section>

      <Card className="sm-workbench-card sm-workbench-quick" title="快速发起">
        <div className="sm-workbench-quick-grid">
          {quickActions.map((action) => (
            <button
              key={action.key}
              type="button"
              className="sm-workbench-quick-item"
              onClick={showSampleNotice}
            >
              <span className="sm-workbench-quick-icon">{action.icon}</span>
              <span>{action.label}</span>
            </button>
          ))}
        </div>
      </Card>

      <section className="sm-workbench-metrics">
        <Card className="sm-workbench-card sm-workbench-metric-card">
          <span className="sm-workbench-metric-label">待我审批</span>
          <button type="button" className="sm-workbench-metric-value" onClick={showSampleNotice}>
            12
          </button>
          <span className="sm-workbench-metric-note">其中3项即将超时</span>
        </Card>
        <Card className="sm-workbench-card sm-workbench-metric-card">
          <span className="sm-workbench-metric-label">本月采购申请</span>
          <button type="button" className="sm-workbench-metric-value" onClick={showSampleNotice}>
            286
          </button>
          <span className="sm-workbench-metric-note sm-workbench-metric-note--success">
            较上月增长 8.6%
          </span>
        </Card>
        <Card className="sm-workbench-card sm-workbench-metric-card">
          <span className="sm-workbench-metric-label">已提交</span>
          <button type="button" className="sm-workbench-metric-value" onClick={showSampleNotice}>
            175
          </button>
          <span className="sm-workbench-metric-note">提交率 61.2%</span>
        </Card>
        <Card className="sm-workbench-card sm-workbench-metric-card">
          <span className="sm-workbench-metric-label">系统消息</span>
          <button type="button" className="sm-workbench-metric-value" onClick={showSampleNotice}>
            8
          </button>
          <span className="sm-workbench-metric-note">2条重要提醒</span>
        </Card>
      </section>

      <section className="sm-workbench-analysis">
        <Card
          className="sm-workbench-card sm-workbench-trend"
          title="采购申请趋势"
          extra={<Button type="link">近6个月</Button>}
        >
          <div className="sm-workbench-chart">
            <div className="sm-workbench-chart-grid">
              <span>300</span>
              <span>200</span>
              <span>100</span>
              <span>0</span>
            </div>
            <div className="sm-workbench-bars" aria-label="近6个月采购申请趋势">
              <div className="sm-workbench-bar sm-workbench-bar--1">
                <span>2月</span>
              </div>
              <div className="sm-workbench-bar sm-workbench-bar--2">
                <span>3月</span>
              </div>
              <div className="sm-workbench-bar sm-workbench-bar--3">
                <span>4月</span>
              </div>
              <div className="sm-workbench-bar sm-workbench-bar--4">
                <span>5月</span>
              </div>
              <div className="sm-workbench-bar sm-workbench-bar--5">
                <span>6月</span>
              </div>
              <div className="sm-workbench-bar sm-workbench-bar--6">
                <span>7月</span>
              </div>
            </div>
          </div>
        </Card>

        <Card className="sm-workbench-card sm-workbench-progress" title="采购类型占比">
          <div className="sm-workbench-progress-item">
            <div>
              <span>办公及行政</span>
              <span>38%</span>
            </div>
            <Progress percent={38} showInfo={false} />
          </div>
          <div className="sm-workbench-progress-item">
            <div>
              <span>生产物资</span>
              <span>32%</span>
            </div>
            <Progress percent={32} showInfo={false} />
          </div>
          <div className="sm-workbench-progress-item">
            <div>
              <span>IT设备与服务</span>
              <span>19%</span>
            </div>
            <Progress percent={19} showInfo={false} />
          </div>
          <div className="sm-workbench-progress-item">
            <div>
              <span>其他</span>
              <span>11%</span>
            </div>
            <Progress percent={11} showInfo={false} />
          </div>
        </Card>
      </section>

      <section className="sm-workbench-bottom">
        <Card
          className="sm-workbench-card sm-workbench-pending"
          title="待处理采购申请"
          extra={<Button type="link">查看全部</Button>}
        >
          <Table
            columns={pendingColumns}
            dataSource={pendingItems}
            pagination={false}
            size="small"
            tableLayout="fixed"
          />
        </Card>
        <Card className="sm-workbench-card sm-workbench-recent" title="最近使用">
          <ul>
            <li>
              <FileSearchOutlined />
              <span>采购申请列表</span>
              <time>10分钟前</time>
            </li>
            <li>
              <TeamOutlined />
              <span>用户管理</span>
              <time>昨天</time>
            </li>
            <li>
              <AppstoreOutlined />
              <span>应用管理</span>
              <time>7月21日</time>
            </li>
          </ul>
        </Card>
      </section>
    </div>
  );
};

export default SampleWorkbench;
