import { memo } from 'react';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import { useQuery } from '@tanstack/react-query';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { Empty, Spin } from 'antd';
import { fetchApps } from '@/domain/sys/base/app/api';
import type { AppVO } from '@/domain/sys/base/app/types';
import { openApp } from '@/services/navigationService';
import AppCardIcon from './AppCardIcon';
import './AppsView.css';

const AppsView = () => {
  const query = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: appQueryKeys.domainApps(),
    queryFn: fetchApps,
  });
  const { data, isLoading } = query;

  const handleAppClick = (app: AppVO) => {
    openApp(app.number);
  };

  return (
    <div className="sm-apps">
      {getBlockingQueryError(query) ? (
        <RequestErrorState error={query.error} onRetry={() => void query.refetch()} />
      ) : (
        <>
          <Spin spinning={isLoading}>
            {data?.map((domain) => (
              <div key={domain.number} className="sm-domain-group">
                <div className="sm-domain-name">
                  <div className="sm-domain-name-text">{domain.name}</div>
                </div>
                <div className="sm-domain-apps">
                  {domain.appList?.map((app) => (
                    <div
                      key={app.number}
                      className="sm-app-card"
                      onClick={() => handleAppClick(app)}
                    >
                      <AppCardIcon icon={app.icon} iconColor={app.iconColor} />
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
        </>
      )}
    </div>
  );
};

export default memo(AppsView);
