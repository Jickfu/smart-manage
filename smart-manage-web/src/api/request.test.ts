/// <reference types="node" />

import type { AxiosAdapter } from 'axios';
import { beforeAll, describe, expect, it } from 'vitest';

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
      code: 100401,
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

    await expect(request.post('/test', {}, { adapter })).rejects.toMatchObject({ code: 100419 });

    expect(useUserStore.getState().csrfToken).toBe(csrfToken);
    expect(windowStub.location.href).toBe('http://localhost:8000/index.html?app=home');
    expect(notificationCount).toBe(1);
    windowStub.removeEventListener('sm:csrf-invalid', listener);
  });
});
