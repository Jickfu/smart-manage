import { Card, Empty } from 'antd';

const BaseHome = () => (
  <div className="sm-app-home">
    <header className="sm-app-home-header">
      <div>
        <h1>系统管理</h1>
        <p>组织、授权与平台配置</p>
      </div>
    </header>
    <Card className="sm-app-home-card">
      <Empty description="暂无经过独立授权的系统治理概览" />
    </Card>
  </div>
);

export default BaseHome;
