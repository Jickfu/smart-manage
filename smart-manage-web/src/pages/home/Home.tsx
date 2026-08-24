import { memo } from 'react';
import { Card, Empty, Spin } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { fetchApps } from '@/domain/sys/base/app/api';
import { appQueryKeys } from '@/domain/sys/base/app/queryKeys';
import { openApp } from '@/services/navigationService';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { useUserStore } from '@/stores/user';
import { useWorkbenchStore } from '@/stores/workbench';
import './Home.css';

const Home = () => {
  const user = useUserStore((state) => state.userInfo);
  const pinnedNumbers = useHeaderTabsStore((state) =>
    state.tabs.filter((tab) => tab.type === 'app' && tab.pinned).map((tab) => tab.key),
  );
  const workspaces = useWorkbenchStore((state) => state.workspaces);
  const appsQuery = useQuery({ queryKey: appQueryKeys.domainApps(), queryFn: fetchApps });
  const apps = appsQuery.data?.flatMap((domain) => domain.appList) ?? [];
  const pinnedApps = pinnedNumbers
    .map((number) => apps.find((app) => app.number === number))
    .filter(Boolean);
  const recentPages = Object.values(workspaces)
    .flatMap((workspace) =>
      workspace.contentTabs
        .filter((tab) => tab.key !== '__home__')
        .map((tab) => ({
          appName: workspace.appInfo.name,
          label: tab.label,
        })),
    )
    .slice(-6)
    .reverse();

  return (
    <div className="sm-system-home">
      <section className="sm-system-home-welcome">
        <h1>你好，{user?.name || user?.username || '用户'}</h1>
        <p>
          {user?.companyName || 'Smart Manage'} · {user?.currentOrgName || '未设置当前组织'}
        </p>
      </section>
      <Card className="sm-system-home-card" title="常用应用">
        <Spin spinning={appsQuery.isLoading}>
          {pinnedApps.length ? (
            <div className="sm-system-home-apps">
              {pinnedApps.map(
                (app) =>
                  app && (
                    <button key={app.number} type="button" onClick={() => openApp(app.number)}>
                      <strong>{app.name}</strong>
                      <span>{app.description}</span>
                    </button>
                  ),
              )}
            </div>
          ) : (
            <Empty description="尚未固定常用应用，可在应用页签中固定" />
          )}
        </Spin>
      </Card>
      <Card className="sm-system-home-card" title="最近使用">
        {recentPages.length ? (
          <div className="sm-system-home-recent">
            {recentPages.map((page, index) => (
              <div key={`${page.appName}-${page.label}-${index}`}>
                <strong>{page.label}</strong>
                <span>{page.appName}</span>
              </div>
            ))}
          </div>
        ) : (
          <Empty description="暂无最近打开的业务页面" />
        )}
      </Card>
    </div>
  );
};

export default memo(Home);
