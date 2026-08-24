import { Card, Empty } from 'antd';

const MonitorHome = () => (
  <div className="sm-app-home">
    <header className="sm-app-home-header">
      <div>
        <h1>运维中心</h1>
        <p>运行健康与异常关注</p>
      </div>
    </header>
    <Card className="sm-app-home-card">
      <Empty description="请通过左侧菜单进入已授权的实时监控与审计能力" />
    </Card>
  </div>
);

export default MonitorHome;
