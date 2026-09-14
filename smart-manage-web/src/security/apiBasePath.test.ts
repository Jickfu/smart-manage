/// <reference types="node" />

import { createHash } from 'node:crypto';
import { createServer as createHttpServer } from 'node:http';
import { once } from 'node:events';
import { createServer } from 'vite';
import { afterEach, describe, expect, it, vi } from 'vitest';

afterEach(() => vi.unstubAllEnvs());

describe('API 路径配置', () => {
  it.each(['/smart-manage-api', '/custom/api'])(
    '%s 同时用于登录页、模块请求和开发代理，并保持登录脚本摘要有效',
    async (apiBasePath) => {
      vi.stubEnv('VITE_API_BASE_PATH', apiBasePath);
      const backend = createHttpServer((request, response) => {
        response.setHeader('Content-Type', 'application/json');
        response.end(JSON.stringify({ path: request.url }));
      });
      backend.listen(0, '127.0.0.1');
      await once(backend, 'listening');
      const backendAddress = backend.address();
      if (!backendAddress || typeof backendAddress === 'string') throw new Error('测试后端未监听');
      const server = await createServer({
        server: { port: 0, host: '127.0.0.1', open: false },
      });
      try {
        const proxy = server.config.server.proxy?.[apiBasePath];
        expect(proxy).toBeDefined();
        if (!proxy || typeof proxy === 'string') throw new Error('缺少 API 代理配置');
        proxy.target = `http://127.0.0.1:${backendAddress.port}`;
        await server.listen();
        const address = server.httpServer?.address();
        if (!address || typeof address === 'string') throw new Error('开发服务器未监听');
        const origin = `http://127.0.0.1:${address.port}`;
        const html = await (await fetch(`${origin}/login.html`)).text();
        expect(html).toContain(`name="sm-api-base-path" content="${apiBasePath}"`);
        expect(html).not.toContain('/@react-refresh');
        const inlineScript = html.match(/<script>([\s\S]*?)<\/script>/)?.[1];
        expect(inlineScript).toBeDefined();
        const scriptHash = createHash('sha256')
          .update(inlineScript!.replace(/\r\n?/g, '\n'))
          .digest('base64');
        expect(html).toContain(`'sha256-${scriptHash}'`);
        // 经真实开发代理发送请求，确认没有删除或重写 context path。
        const response = await fetch(`${origin}${apiBasePath}/sys/base/session`);
        expect(await response.json()).toEqual({ path: `${apiBasePath}/sys/base/session` });
        const requestModule = await server.transformRequest('/src/api/request.ts');
        const assetModule = await server.transformRequest('/src/utils/assetUrl.ts');
        for (const moduleResult of [requestModule, assetModule]) {
          expect(moduleResult?.code).toContain(`"VITE_API_BASE_PATH": "${apiBasePath}"`);
        }
      } finally {
        await server.close();
        backend.closeAllConnections();
        await new Promise<void>((resolve, reject) => {
          backend.close((error) => (error ? reject(error) : resolve()));
        });
      }
    },
    20000,
  );
});
