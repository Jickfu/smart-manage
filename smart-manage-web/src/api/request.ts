import axios from 'axios';
import type { Result } from '@/types/api';
import { useUserStore } from '@/stores/user';
import { ApiError } from './ApiError';

const SUCCESS_CODE = 0;
const UNAUTHORIZED_CODE = 100401;
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

/** 响应拦截器 - 统一错误处理，保留完整业务错误信息 */
request.interceptors.response.use(
  (response) => {
    // 文件下载和受保护图片返回二进制内容，不使用 Result<T> 包装。
    // 必须在业务码判断前直接放行，否则 Blob 没有 code 字段，会被误判为失败。
    if (response.data instanceof Blob || response.config.responseType === 'blob') {
      return response;
    }
    const result = response.data as Result;
    if (result.code !== SUCCESS_CODE) {
      // 未登录，跳转登录页。
      if (result.code === UNAUTHORIZED_CODE) {
        clearAuthentication();
        const redirectUrl = encodeURIComponent(window.location.href);
        window.location.href = `/login.html?redirect=${redirectUrl}`;
      }
      if (result.code === CSRF_TOKEN_INVALID_CODE) {
        window.dispatchEvent(new CustomEvent('sm:csrf-invalid'));
      }
      return Promise.reject(
        new ApiError(result.code, result.msg, result.traceId ?? '', result.data),
      );
    }
    return response;
  },
  (error) => {
    // 网络错误 / HTTP 错误 - 尝试从响应体中提取业务错误信息。
    if (axios.isAxiosError(error) && error.response) {
      const httpStatus = error.response.status;
      const result = error.response.data as Result | undefined;

      // HTTP 401 - 登录跳转。
      if (httpStatus === 401) {
        clearAuthentication();
        const redirectUrl = encodeURIComponent(window.location.href);
        window.location.href = `/login.html?redirect=${redirectUrl}`;
      }

      // 如果响应体包含 Result 结构，保留完整信息。
      if (result && typeof result.code === 'number') {
        return Promise.reject(
          new ApiError(result.code, result.msg, result.traceId ?? '', result.data),
        );
      }

      // HTTP 错误但无 Result 结构，例如网关错误。
      return Promise.reject(new ApiError(httpStatus, error.message, '', undefined));
    }

    // 完全无法识别的错误。
    if (error instanceof ApiError) {
      return Promise.reject(error);
    }
    return Promise.reject(new ApiError(-1, error?.message ?? '网络异常', '', undefined));
  },
);

export default request;
