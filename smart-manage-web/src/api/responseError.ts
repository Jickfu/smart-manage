import axios, { type AxiosResponse } from 'axios';
import { ApiError } from './ApiError';

interface ResultEnvelope {
  code: number;
  msg: string;
  data: unknown;
  traceId?: string | null;
  feedbackLevel?: unknown;
}

function isResultEnvelope(value: unknown): value is ResultEnvelope {
  if (!value || typeof value !== 'object' || Array.isArray(value)) return false;
  const body = value as Record<string, unknown>;
  return (
    Object.hasOwn(body, 'code') &&
    Number.isSafeInteger(body.code) &&
    Object.hasOwn(body, 'msg') &&
    typeof body.msg === 'string' &&
    Object.hasOwn(body, 'data') &&
    (!Object.hasOwn(body, 'traceId') || body.traceId === null || typeof body.traceId === 'string')
  );
}

function isFileDisposition(disposition: string): boolean {
  return (
    /^attachment(?:\s*;|\s*$)/i.test(disposition) ||
    (/^inline\s*;/i.test(disposition) && /;\s*filename\*?\s*=/i.test(disposition))
  );
}

/** 只解析受控 JSON 错误体，不嗅探文件正文，不无界读取可能很大的下载。 */
export async function getResponseError(response: AxiosResponse): Promise<ApiError | null> {
  const httpStatus = response.status;
  const httpSuccess = httpStatus >= 200 && httpStatus < 300;
  let body: unknown = response.data;
  const blobMode = body instanceof Blob || response.config.responseType === 'blob';
  if (body instanceof Blob) {
    const contentType = String(response.headers['content-type'] ?? body.type)
      .split(';')[0]!
      .trim();
    const disposition = String(response.headers['content-disposition'] ?? '');
    if (httpSuccess && isFileDisposition(disposition)) return null;
    if (/^application\/(?:[\w.+-]+\+)?json$/i.test(contentType)) {
      if (body.size <= 64 * 1024) {
        try {
          body = JSON.parse(await body.text());
        } catch {
          body = null;
        }
      } else {
        body = null;
      }
      // Blob.text 是异步读取；取消可能发生在 Axios 完成响应转换之后。
      response.config.cancelToken?.throwIfRequested();
      if (response.config.signal?.aborted) throw new axios.CanceledError();
    } else if (httpSuccess) {
      return null;
    }
  }
  if (isResultEnvelope(body)) {
    if (body.code !== 0) {
      return new ApiError({
        source: 'API',
        httpStatus,
        apiCode: body.code,
        message: body.msg,
        feedbackLevel: body.feedbackLevel === 'WARNING' ? 'WARNING' : 'ERROR',
        traceId: body.traceId,
        data: body.data,
      });
    }
    if (httpSuccess && !blobMode) return null;
  }
  return new ApiError({
    source: httpSuccess ? 'PROTOCOL' : 'HTTP',
    httpStatus,
    message: httpSuccess
      ? '服务器响应格式异常，请联系管理员'
      : `服务器请求失败（HTTP ${httpStatus}），请稍后重试`,
  });
}

/** 无响应时只识别确定的传输原因；配置或本地转换错误不是网络中断。 */
export function getTransportError(error: unknown): unknown {
  if (axios.isCancel(error) || error instanceof ApiError) return error;
  if (axios.isAxiosError(error)) {
    if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      return new ApiError({ source: 'TIMEOUT', message: '请求超时，请稍后重试' });
    }
    if (error.code === 'ERR_NETWORK' || error.request) {
      return new ApiError({ source: 'NETWORK', message: '未能连接到服务器，请检查网络或稍后重试' });
    }
  }
  return new ApiError({ source: 'CLIENT', message: '请求处理失败，请稍后重试' });
}
