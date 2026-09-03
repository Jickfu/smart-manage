import axios from 'axios';
import { useUserStore } from '@/stores/user';
import { ApiError } from './ApiError';
import { getResponseError, getTransportError } from './responseError';
import { isAuthenticationError } from './errorPresentation';

const CSRF_TOKEN_INVALID_CODE = 100419;
const CSRF_HEADER_NAME = 'sm-csrf-token';
const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

const request = axios.create({
  baseURL: '/smart-manage-api',
  timeout: 30000,
});

/** 清理前端会话快照；HttpOnly Cookie 只能由服务端失效或删除。 */
function clearAuthentication() {
  useUserStore.getState().clearUser();
}

/** 已认证的非安全请求统一提交会话绑定的 CSRF Token。 */
request.interceptors.request.use((config) => {
  const method = (config.method ?? 'GET').toUpperCase();
  const csrfToken = useUserStore.getState().csrfToken;
  if (!SAFE_METHODS.has(method) && csrfToken) {
    config.headers[CSRF_HEADER_NAME] = csrfToken;
  }
  return config;
});

/** 只路由认证/安全事件，不在 Axios 层弹出普通错误提示。 */
function routeSecurityError(error: unknown) {
  if (isAuthenticationError(error)) {
    clearAuthentication();
    if (!window.location.pathname?.endsWith('/login.html')) {
      const redirectUrl = encodeURIComponent(window.location.href);
      window.location.href = `/login.html?redirect=${redirectUrl}`;
    }
  } else if (error instanceof ApiError && error.apiCode === CSRF_TOKEN_INVALID_CODE) {
    window.dispatchEvent(new CustomEvent('sm:csrf-invalid'));
  }
}

/** 响应始终校验真实 HTTP 状态，不能因调用方 validateStatus 放宽或 body.code=0 吞掉失败。 */
request.interceptors.response.use(
  async (response) => {
    const error = await getResponseError(response);
    if (error) {
      routeSecurityError(error);
      throw error;
    }
    return response;
  },
  async (error: unknown) => {
    if (axios.isCancel(error)) throw error;
    const normalized =
      axios.isAxiosError(error) && error.response
        ? ((await getResponseError(error.response)) ?? getTransportError(error))
        : getTransportError(error);
    routeSecurityError(normalized);
    throw normalized;
  },
);

export default request;
