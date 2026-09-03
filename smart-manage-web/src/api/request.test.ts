/// <reference types="node" />

import axios, { AxiosError, type AxiosAdapter } from 'axios';
import { beforeAll, describe, expect, it, vi } from 'vitest';

const windowEvents = new EventTarget();
const windowStub = {
  location: {
    href: 'http://localhost:8000/index.html?app=home',
  },
  addEventListener: windowEvents.addEventListener.bind(windowEvents),
  removeEventListener: windowEvents.removeEventListener.bind(windowEvents),
  dispatchEvent: windowEvents.dispatchEvent.bind(windowEvents),
};

let request: typeof import('./request').default;
let useUserStore: typeof import('@/stores/user').useUserStore;

beforeAll(async () => {
  Object.defineProperty(globalThis, 'window', { value: windowStub });
  request = (await import('./request')).default;
  useUserStore = (await import('@/stores/user')).useUserStore;
});

describe('request authentication handling', () => {
  it('passes through successful blob responses without Result parsing', async () => {
    const image = new Blob(['image-content'], { type: 'image/jpeg' });
    const adapter: AxiosAdapter = async (config) => ({
      data: image,
      status: 200,
      statusText: 'OK',
      headers: { 'content-type': 'image/jpeg' },
      config,
    });

    await expect(
      request.get('/protected-image', { adapter, responseType: 'blob' }),
    ).resolves.toMatchObject({ data: image, status: 200 });
  });

  it('clears the in-memory session before redirecting on business 401', async () => {
    useUserStore.setState({ csrfToken: 'csrf-token', userInfo: null });
    windowStub.location.href = 'http://localhost:8000/index.html?app=home';

    const adapter: AxiosAdapter = async (config) => ({
      data: { code: 100401, msg: '登录已失效', data: null },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    });

    await expect(request.get('/test', { adapter })).rejects.toMatchObject({
      apiCode: 100401,
      message: '登录已失效',
    });
    expect(useUserStore.getState().csrfToken).toBeNull();
    expect(windowStub.location.href).toContain('/login.html?redirect=');
  });

  it('adds the CSRF header only to non-safe requests', async () => {
    useUserStore.setState({ csrfToken: '0123456789abcdef0123456789abcdef' });
    const adapter: AxiosAdapter = async (config) => ({
      data: { code: 0, msg: '', data: null },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    });

    const postResponse = await request.post('/test', {}, { adapter });
    const getResponse = await request.get('/test', { adapter });

    expect(postResponse.config.headers['sm-csrf-token']).toBe('0123456789abcdef0123456789abcdef');
    expect(getResponse.config.headers['sm-csrf-token']).toBeUndefined();
  });

  it('keeps the session and emits a security notification on CSRF failure', async () => {
    const csrfToken = '0123456789abcdef0123456789abcdef';
    let notificationCount = 0;
    const listener = () => notificationCount++;
    windowStub.addEventListener('sm:csrf-invalid', listener);
    useUserStore.setState({ csrfToken });
    windowStub.location.href = 'http://localhost:8000/index.html?app=home';
    const adapter: AxiosAdapter = async (config) => ({
      data: { code: 100419, msg: '安全校验失败，请刷新页面后重试', data: null },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    });

    await expect(request.post('/test', {}, { adapter })).rejects.toMatchObject({ apiCode: 100419 });

    expect(useUserStore.getState().csrfToken).toBe(csrfToken);
    expect(windowStub.location.href).toBe('http://localhost:8000/index.html?app=home');
    expect(notificationCount).toBe(1);
    windowStub.removeEventListener('sm:csrf-invalid', listener);
  });
});

function responseAdapter(
  data: unknown,
  status = 200,
  headers: Record<string, string> = {},
): AxiosAdapter {
  return async (config) => {
    const response = { data, status, statusText: '', headers, config };
    if (status >= 400)
      throw new AxiosError('raw server detail', 'ERR_BAD_RESPONSE', config, {}, response);
    return response;
  };
}

const failureBody = {
  code: 100422,
  msg: '参数异常',
  data: null,
  traceId: 'trace-1',
  feedbackLevel: 'WARNING',
};

describe('request failure protocol', () => {
  it('rejects non-2xx even when a custom adapter resolves it', async () => {
    const adapter: AxiosAdapter = async (config) => ({
      data: { code: 0, msg: '', data: null },
      status: 500,
      statusText: '',
      headers: {},
      config,
    });
    await expect(
      request.get('/test', { adapter, validateStatus: () => true }),
    ).rejects.toMatchObject({ source: 'HTTP', httpStatus: 500 });
  });

  it('does not read oversized JSON or ordinary binary bodies', async () => {
    const oversized = new Blob(['x'.repeat(65537)], { type: 'application/json' });
    const binary = new Blob(['file'], { type: 'application/octet-stream' });
    const oversizedRead = vi.spyOn(oversized, 'text');
    const binaryRead = vi.spyOn(binary, 'text');
    await expect(
      request.get('/file', { adapter: responseAdapter(oversized), responseType: 'blob' }),
    ).rejects.toMatchObject({ source: 'PROTOCOL' });
    await expect(
      request.get('/file', { adapter: responseAdapter(binary), responseType: 'blob' }),
    ).resolves.toMatchObject({ data: binary });
    expect(oversizedRead).not.toHaveBeenCalled();
    expect(binaryRead).not.toHaveBeenCalled();
  });

  it('preserves cancellation that occurs during JSON Blob reading', async () => {
    const controller = new AbortController();
    const body = new Blob([JSON.stringify(failureBody)], { type: 'application/json' });
    vi.spyOn(body, 'text').mockImplementation(async () => {
      controller.abort();
      return JSON.stringify(failureBody);
    });
    const outcome = await request
      .get('/file', {
        adapter: responseAdapter(body),
        responseType: 'blob',
        signal: controller.signal,
      })
      .catch((error: unknown) => error);
    expect(axios.isCancel(outcome)).toBe(true);
  });

  it.each([200, 422, 500])(
    'preserves API code and actual HTTP %i independently',
    async (status) => {
      await expect(
        request.get('/test', { adapter: responseAdapter(failureBody, status) }),
      ).rejects.toMatchObject({
        source: 'API',
        httpStatus: status,
        apiCode: 100422,
        feedbackLevel: 'WARNING',
        traceId: 'trace-1',
      });
    },
  );

  it.each([undefined, null, 'SUCCESS', 1])(
    'unknown feedback hint %s keeps API identity and defaults ERROR',
    async (feedbackLevel) => {
      await expect(
        request.get('/test', { adapter: responseAdapter({ ...failureBody, feedbackLevel }) }),
      ).rejects.toMatchObject({
        source: 'API',
        apiCode: 100422,
        feedbackLevel: 'ERROR',
      });
    },
  );

  it.each([
    null,
    '<html>gateway</html>',
    [],
    {},
    { code: '0', msg: '', data: null },
    { code: 0.5, msg: '', data: null },
    { code: 0, data: null },
    { code: 0, msg: '' },
    { ...failureBody, traceId: 123 },
  ])('rejects malformed 2xx envelope %j', async (body) => {
    await expect(request.get('/test', { adapter: responseAdapter(body) })).rejects.toMatchObject({
      source: 'PROTOCOL',
      httpStatus: 200,
    });
  });

  it('requires own core properties', async () => {
    const inherited = Object.create({ code: 0, msg: '', data: null });
    await expect(
      request.get('/test', { adapter: responseAdapter(inherited) }),
    ).rejects.toMatchObject({ source: 'PROTOCOL' });
  });

  it.each([null, '<html>private detail</html>', { code: 0, msg: '', data: null }])(
    'never accepts HTTP failure even with success body %j',
    async (body) => {
      await expect(
        request.get('/test', { adapter: responseAdapter(body, 502) }),
      ).rejects.toMatchObject({ source: 'HTTP', httpStatus: 502, apiCode: undefined });
    },
  );

  it.each(['ECONNABORTED', 'ETIMEDOUT', 'ERR_NETWORK', 'ERR_BAD_OPTION'])(
    'classifies transport code %s without exposing raw message',
    async (code) => {
      const adapter: AxiosAdapter = async () => {
        throw new AxiosError('sensitive internal error', code);
      };
      await expect(request.get('/test', { adapter })).rejects.toMatchObject({
        source:
          code === 'ERR_NETWORK' ? 'NETWORK' : code === 'ERR_BAD_OPTION' ? 'CLIENT' : 'TIMEOUT',
      });
      await expect(request.get('/test', { adapter })).rejects.not.toHaveProperty(
        'message',
        'sensitive internal error',
      );
    },
  );

  it('preserves cancellation identity', async () => {
    const canceled = new axios.CanceledError('canceled');
    const adapter: AxiosAdapter = async () => {
      throw canceled;
    };
    await expect(request.get('/test', { adapter })).rejects.toBe(canceled);
  });

  it.each([200, 403])('parses JSON Blob failure at HTTP %i', async (status) => {
    const body = new Blob([JSON.stringify(failureBody)], { type: 'application/json' });
    await expect(
      request.get('/download', { responseType: 'blob', adapter: responseAdapter(body, status) }),
    ).rejects.toMatchObject({ source: 'API', httpStatus: status, apiCode: 100422 });
  });

  it.each(['attachment; filename="data.json"', "inline; filename*=UTF-8''data.json"])(
    'preserves real JSON files with %s',
    async (disposition) => {
      const body = new Blob([JSON.stringify(failureBody)], { type: 'application/json' });
      await expect(
        request.get('/download', {
          responseType: 'blob',
          adapter: responseAdapter(body, 200, { 'content-disposition': disposition }),
        }),
      ).resolves.toMatchObject({ data: body });
    },
  );

  it('does not exempt bare inline or any non-2xx file header', async () => {
    const body = new Blob([JSON.stringify(failureBody)], { type: 'application/problem+json' });
    for (const status of [200, 500]) {
      await expect(
        request.get('/download', {
          responseType: 'blob',
          adapter: responseAdapter(body, status, {
            'content-disposition': status === 200 ? 'inline' : 'attachment; filename="error.json"',
          }),
        }),
      ).rejects.toMatchObject({ source: 'API', httpStatus: status });
    }
  });

  it.each([200, 500])(
    'bounds JSON Blob parsing and keeps HTTP %i classification',
    async (status) => {
      for (const content of [
        '{invalid',
        JSON.stringify({ code: 0, msg: '', data: null }),
        'x'.repeat(65537),
      ]) {
        const body = new Blob([content], { type: 'application/json' });
        await expect(
          request.get('/download', {
            responseType: 'blob',
            adapter: responseAdapter(body, status),
          }),
        ).rejects.toMatchObject({ source: status === 200 ? 'PROTOCOL' : 'HTTP' });
      }
    },
  );

  it('routes business authentication and CSRF in non-2xx responses', async () => {
    useUserStore.setState({ csrfToken: 'csrf-token' });
    await expect(
      request.get('/test', { adapter: responseAdapter({ ...failureBody, code: 100401 }, 403) }),
    ).rejects.toMatchObject({ apiCode: 100401 });
    expect(useUserStore.getState().csrfToken).toBeNull();
    let count = 0;
    const listener = () => count++;
    windowStub.addEventListener('sm:csrf-invalid', listener);
    await expect(
      request.get('/test', { adapter: responseAdapter({ ...failureBody, code: 100419 }, 403) }),
    ).rejects.toMatchObject({ apiCode: 100419 });
    expect(count).toBe(1);
    windowStub.removeEventListener('sm:csrf-invalid', listener);
  });
});
