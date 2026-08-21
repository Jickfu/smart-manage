import { useCallback, useEffect, useMemo, useState } from 'react';
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider, App as AntApp, Button, Result, Spin } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import routes from '@/router';
import { createThemeConfig } from '@/styles/theme';
import { getCurrentSession } from '@/api/user';
import { ApiError } from '@/api/ApiError';
import { useUserStore } from '@/stores/user';
// 自动生成的组件注册表导入 — 由 pnpm gen:registry 生成
import '@/domain/common/registry/registry.gen';
import { AppErrorBoundary } from '@/pages/errors/AppErrorBoundary';

const UNAUTHORIZED_CODE = 100401;

/** 认证状态 — 启动时由服务端 Cookie 会话恢复。 */
type AuthState = 'loading' | 'authenticated' | 'error';

function SecurityErrorNotifier() {
  const { message } = AntApp.useApp();

  useEffect(() => {
    const notify = () => message.error('安全校验失败，请刷新页面后重试');
    window.addEventListener('sm:csrf-invalid', notify);
    return () => window.removeEventListener('sm:csrf-invalid', notify);
  }, [message]);

  return null;
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  const router = useMemo(() => createMemoryRouter(routes), []);
  const setSession = useUserStore((s) => s.setSession);
  const themeColor = useUserStore((s) => s.userInfo?.themeColor);
  const [authState, setAuthState] = useState<AuthState>('loading');
  const themeConfig = useMemo(() => createThemeConfig(themeColor), [themeColor]);

  /** 处理认证 API 响应 — 成功存用户信息，失败区分 401 与网络错误 */
  const handleAuthResult = useCallback(
    (res: Awaited<ReturnType<typeof getCurrentSession>>) => {
      const info = res.data.user;
      setSession(
        {
          id: String(info.id),
          username: info.username,
          name: info.name,
          avatar: info.avatar,
          avatarAttachmentId: info.avatarAttachmentId ? String(info.avatarAttachmentId) : undefined,
          themeColor: info.themeColor,
          number: info.number,
          email: info.email,
          phone: info.phone,
          currentOrgId: String(info.currentOrgId),
          currentOrgName: info.currentOrgName,
          companyName: info.companyName,
          assignments: info.assignments.map((assignment) => ({
            ...assignment,
            id: String(assignment.id),
            org: { ...assignment.org, id: String(assignment.org.id) },
          })),
        },
        res.data.csrfToken,
      );
      setAuthState('authenticated');
    },
    [setSession],
  );

  const handleAuthError = useCallback((err: unknown) => {
    // 401 由 request.ts 拦截器处理跳转，此处保持 loading 避免闪屏
    if (err instanceof ApiError && err.code === UNAUTHORIZED_CODE) {
      return;
    }
    setAuthState('error');
  }, []);

  /** 重试认证 — 从错误页手动触发，需先切回 loading 状态 */
  const retryAuth = useCallback(() => {
    setAuthState('loading');
    getCurrentSession().then(handleAuthResult).catch(handleAuthError);
  }, [handleAuthResult, handleAuthError]);

  /** 启动时直接校验 HttpOnly Cookie；前端不读取或推断认证凭证。 */
  useEffect(() => {
    getCurrentSession().then(handleAuthResult).catch(handleAuthError);
  }, [handleAuthResult, handleAuthError]);

  // 认证检查中 — 全屏居中加载
  if (authState === 'loading') {
    return (
      <div className="sm-auth-container">
        <Spin size="large" />
      </div>
    );
  }

  // 网络错误 — 无法连接后端，提供重试入口
  if (authState === 'error') {
    return (
      <Result
        status="warning"
        title="无法连接到服务器"
        subTitle="请检查网络连接或确认后端服务已启动"
        extra={
          <Button type="primary" onClick={retryAuth}>
            重试
          </Button>
        }
      />
    );
  }

  // 认证通过 — 正常渲染应用
  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider theme={themeConfig} locale={zhCN}>
        <AntApp>
          <SecurityErrorNotifier />
          <AppErrorBoundary>
            <RouterProvider router={router} />
          </AppErrorBoundary>
        </AntApp>
      </ConfigProvider>
    </QueryClientProvider>
  );
}
