import { memo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { Empty, Spin } from 'antd';
import { AppstoreOutlined } from '@ant-design/icons';
import { fetchApps } from '@/domain/sys/base/app/api';
import type { AppVO } from '@/domain/sys/base/app/types';
import { openApp } from '@/services/navigationService';
import './AppsView.css';

const AppsView = () => {
  const { data, isLoading } = useQuery({
    queryKey: appQueryKeys.cloudApps(),
    queryFn: fetchApps,
  });

  const handleAppClick = (app: AppVO) => {
    openApp(app.number);
  };

  return (
    <div className="sm-apps">
      <Spin spinning={isLoading}>
        {data?.map((cloud) => (
          <div key={cloud.number} className="sm-cloud-domain">
            <div className="sm-cloud-name">
              <div className="sm-cloud-name-text">{cloud.name}</div>
            </div>
            <div className="sm-cloud-apps">
              {cloud.appList?.map((app) => (
                <div key={app.number} className="sm-app-card" onClick={() => handleAppClick(app)}>
                  <AppstoreOutlined className="sm-app-card-icon" />
                  <div className="sm-app-card-text">
                    <div className="sm-app-card-name">{app.name}</div>
                    <div className="sm-app-card-desc">{app.description}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
        {!isLoading && (!data || data.length === 0) && (
          <div className="sm-apps-empty">
            <Empty description="暂无可用应用" />
          </div>
        )}
      </Spin>
    </div>
  );
};

export default memo(AppsView);
