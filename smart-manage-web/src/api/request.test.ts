/// <reference types="node" />

import type { AxiosAdapter } from 'axios';
import { beforeAll, describe, expect, it } from 'vitest';

const storage = new Map<string, string>();
const localStorageStub = {
  getItem: (key: string) => storage.get(key) ?? null,
  setItem: (key: string, value: string) => storage.set(key, value),
  removeItem: (key: string) => storage.delete(key),
  clear: () => storage.clear(),
};
const windowStub = {
  location: {
    href: 'http://localhost:8000/index.html?app=home',
  },
};

let request: typeof import('./request').default;

beforeAll(async () => {
  Object.defineProperty(globalThis, 'localStorage', { value: localStorageStub });
  Object.defineProperty(globalThis, 'window', { value: windowStub });
  request = (await import('./request')).default;
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

  it('clears local authentication before redirecting on business 401', async () => {
    localStorage.setItem('token', 'expired-token');
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
    expect(localStorage.getItem('token')).toBeNull();
    expect(windowStub.location.href).toContain('/login.html?redirect=');
  });
});
