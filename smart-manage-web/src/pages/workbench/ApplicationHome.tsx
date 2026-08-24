import { Card, Empty } from 'antd';
import { resolveApplicationHome } from './applicationHomeRegistry';
import './ApplicationHome.css';

interface ApplicationHomeProps {
  appNumber: string;
  appName: string;
}

const ApplicationHome = ({ appNumber, appName }: ApplicationHomeProps) => {
  const home = resolveApplicationHome(appNumber);
  if (home) return home;
  return (
    <div className="sm-app-home">
      <header className="sm-app-home-header">
        <div>
          <h1>{appName}首页</h1>
          <p>当前应用暂未配置专属首页</p>
        </div>
      </header>
      <Card className="sm-app-home-card">
        <Empty description="暂无首页内容" />
      </Card>
    </div>
  );
};

export default ApplicationHome;
