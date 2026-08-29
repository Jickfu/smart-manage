import { Card, Empty } from 'antd';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import { resolveApplicationHome } from './applicationHomeRegistry';
import './ApplicationHome.css';

interface ApplicationHomeProps {
  appNumber: string;
}

const ApplicationHome = ({ appNumber }: ApplicationHomeProps) => {
  const home = resolveApplicationHome(appNumber);
  if (home) return home;
  return (
    <div className="sm-app-home">
      <QuickLaunchCard scope="APPLICATION" appNumber={appNumber} />
      <Card className="sm-app-home-card">
        <Empty description="暂无首页内容" />
      </Card>
    </div>
  );
};

export default ApplicationHome;
