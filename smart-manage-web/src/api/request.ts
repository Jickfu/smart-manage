import axios from 'axios';
import type { Result } from '@/types/api';
import { useUserStore } from '@/stores/user';
import { ApiError } from './ApiError';

const SUCCESS_CODE = 0;
const UNAUTHORIZED_CODE = 100401;

const request = axios.create({
  baseURL: '/smart-manage-api',
  timeout: 30000,
});

/** 清理浏览器中的失效认证状态，避免 401 后继续携带旧 token。 */
function clearAuthentication() {
  useUserStore.getState().clearUser();
}

/** 请求拦截器 - 注入 Sa-Token */
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers['smtoken'] = token;
  }
  return config;
});

/** 响应拦截器 - 统一错误处理，保留完整业务错误信息 */
request.interceptors.response.use(
  (response) => {
    const result = response.data as Result;
    if (result.code !== SUCCESS_CODE) {
      // 未登录，跳转登录页。
      if (result.code === UNAUTHORIZED_CODE) {
        clearAuthentication();
        const redirectUrl = encodeURIComponent(window.location.href);
        window.location.href = `/login.html?redirect=${redirectUrl}`;
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
