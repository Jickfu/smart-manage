import { lazy, Suspense, useEffect, useRef } from 'react';
import type { ReactNode } from 'react';
import { Layout, Spin } from 'antd';
import Header from './Header';
import { useHeaderTabsStore } from '@/stores/headerTabs';
import { openApp } from '@/services/navigationService';
import { fetchPinnedApps } from '@/domain/sys/base/user/appPinApi';
import './MainLayout.css';

const { Content } = Layout;

const Home = lazy(() => import('@/pages/home/Home'));
const AppsView = lazy(() => import('@/pages/app/AppsView'));
const Workbench = lazy(() => import('@/pages/workbench/Workbench'));

/** Suspense fallback — 页面懒加载时显示 */
const renderLazyPage = (node: ReactNode) => (
  <Suspense
    fallback={
      <div className="sm-view-loading">
        <Spin />
      </div>
    }
  >
    {node}
  </Suspense>
);

const PersistentView = ({ appKey, children }: { appKey: string; children: ReactNode }) => {
  const active = useHeaderTabsStore((state) => state.activeKey === appKey);
  return <li className={`sm-view ${active ? 'sm-view--active' : ''}`}>{children}</li>;
};

const MainLayout = () => {
  const initialAppOpened = useRef(false);
  const tabs = useHeaderTabsStore((s) => s.tabs);
  const loadedAppTabs = tabs.filter((tab) => tab.type === 'app' && tab.loaded);

  useEffect(() => {
    if (initialAppOpened.current) return;
    initialAppOpened.current = true;
    const initialize = async () => {
      try {
        const pinnedApps = await fetchPinnedApps();
        useHeaderTabsStore.getState().initializePinnedApps(pinnedApps);
      } catch {
        // 固定配置加载失败不能阻断 URL 指定应用及基础页面启动。
      }
      const appNumber = new URLSearchParams(window.location.search).get('app')?.trim() || 'home';
      await openApp(appNumber);
    };
    void initialize();
  }, []);

  return (
    <Layout className="sm-layout">
      <Header />
      <Content className="sm-layout-content">
        <ol className="sm-views">
          {/* 首页 */}
          <PersistentView appKey="home">{renderLazyPage(<Home />)}</PersistentView>

          {/* 应用选择页 */}
          <PersistentView appKey="apps">{renderLazyPage(<AppsView />)}</PersistentView>

          {/* 动态应用工作台 — 每个已打开的应用一个 li */}
          {loadedAppTabs.map((tab) => (
            <PersistentView key={tab.key} appKey={tab.key}>
              {renderLazyPage(<Workbench appNumber={tab.key} />)}
            </PersistentView>
          ))}
        </ol>
      </Content>
    </Layout>
  );
};

export default MainLayout;
