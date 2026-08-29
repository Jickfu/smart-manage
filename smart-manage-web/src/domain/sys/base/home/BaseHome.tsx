import { Card, Tag } from 'antd';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';

const BaseHome = () => (
  <div className="sm-app-home">
    <QuickLaunchCard scope="APPLICATION" appNumber="base" />
    <HomeCardGrid>
      <Card className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-1">
        <div className="sm-app-home-metric-title">
          <span>启用用户</span>
          <Tag color="blue">示例</Tag>
        </div>
        <div className="sm-app-home-metric-value">128</div>
        <div className="sm-app-home-metric-caption">当前可登录平台的用户</div>
      </Card>
      <Card className="sm-app-home-card sm-app-home-metric-card sm-home-card-span-3">
        <div className="sm-app-home-metric-title">
          <span>组织与授权</span>
          <Tag color="blue">示例</Tag>
        </div>
        <div className="sm-app-home-metric-pairs">
          <div>
            <strong>12</strong>
            <span>组织单元</span>
          </div>
          <div>
            <strong>18</strong>
            <span>授权角色</span>
          </div>
        </div>
      </Card>
    </HomeCardGrid>
  </div>
);

export default BaseHome;
