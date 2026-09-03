import axios from 'axios';
import { ApiError, type ErrorSource } from './ApiError';

export function getErrorSource(error: unknown): ErrorSource {
  if (axios.isCancel(error)) return 'CANCELED';
  return error instanceof ApiError ? error.source : 'CLIENT';
}

export function isAuthenticationError(error: unknown): boolean {
  return error instanceof ApiError && (error.httpStatus === 401 || error.apiCode === 100401);
}

/** 取消不展示；登录跳转与 CSRF 通知各有唯一 owner，普通反馈不得再次弹出。 */
export function isErrorFeedbackSuppressed(error: unknown): boolean {
  return (
    getErrorSource(error) === 'CANCELED' ||
    isAuthenticationError(error) ||
    (error instanceof ApiError && error.apiCode === 100419)
  );
}

/** 只解释错误，不负责选择页面、提示或弹框。底层异常正文不进入用户界面。 */
export function getErrorPresentation(error: unknown, fallbackMessage = '操作失败，请稍后重试') {
  const source = getErrorSource(error);
  let message = fallbackMessage;
  let type: 'warning' | 'error' = 'error';
  let traceId = '';
  if (error instanceof ApiError) {
    // 诊断 ID 只接受受控字符，禁止把任意响应正文作为诊断信息展示。
    if (/^[A-Za-z0-9._-]{1,64}$/.test(error.traceId)) traceId = error.traceId;
    switch (source) {
      case 'API':
        message = error.message.trim() || fallbackMessage;
        if (error.apiCode === 100409) message = '数据已被其他请求修改，请刷新后重试';
        if (
          error.feedbackLevel === 'WARNING' &&
          (error.httpStatus ?? 200) < 500 &&
          error.httpStatus !== 401 &&
          error.httpStatus !== 403 &&
          error.apiCode !== 100401 &&
          error.apiCode !== 100403 &&
          error.apiCode !== 100419
        ) {
          type = 'warning';
        }
        break;
      case 'HTTP':
        message = `服务器请求失败（HTTP ${error.httpStatus}），请稍后重试`;
        break;
      case 'NETWORK':
        message = '未能连接到服务器，请检查网络或稍后重试';
        break;
      case 'TIMEOUT':
        message = '请求超时，请稍后重试';
        break;
      case 'PROTOCOL':
        message = '服务器响应格式异常，请联系管理员';
        break;
    }
  }
  return { source, message, type, traceId, suppressed: isErrorFeedbackSuppressed(error) };
}
