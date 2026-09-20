import { fileURLToPath, URL } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import packageInfo from './package.json';
import { assetFileName, chunkFileName } from './scripts/build-output';
import { buildAudit } from './scripts/build-audit';

const apiProxyTarget = 'http://localhost:8080';

const browserCryptoStub = {
  name: 'browser-crypto-stub',
  enforce: 'pre' as const,
  resolveId(source: string) {
    // sm-crypto 在浏览器中使用 Web Crypto，此处排除仅供 Node 环境使用的回退分支。
    return source === 'crypto' ? '\0browser-crypto-stub' : null;
  },
  load(id: string) {
    return id === '\0browser-crypto-stub' ? 'export default {};' : null;
  },
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('.', import.meta.url)), 'VITE_');
  const apiBasePath = env.VITE_API_BASE_PATH;
  // API 使用独立的同源路径前缀，不能用根路径代理吞掉登录页和静态资源。
  if (!apiBasePath || !/^\/(?:[A-Za-z0-9_-]+\/)*[A-Za-z0-9_-]+$/.test(apiBasePath)) {
    throw new Error(
      'VITE_API_BASE_PATH 必须是以 / 开头且无尾斜杠的同源路径，路径段仅支持字母、数字、_、-',
    );
  }
  const reactPlugins = react().map((plugin) => {
    const htmlHook = plugin.transformIndexHtml;
    if (!htmlHook) return plugin;
    const handler = typeof htmlHook === 'function' ? htmlHook : htmlHook.handler;
    return {
      ...plugin,
      transformIndexHtml: {
        ...(typeof htmlHook === 'object' ? htmlHook : {}),
        handler(html, context) {
          // 独立登录页不运行 React，也不注入会被其严格 CSP 拒绝的热更新内联脚本。
          if (context.path === '/login.html') return;
          return handler.call(this, html, context);
        },
      },
    } satisfies import('vite').Plugin;
  });
  return {
    define: {
      __PRODUCT_VERSION__: JSON.stringify(packageInfo.version),
    },
    plugins: [browserCryptoStub, reactPlugins, buildAudit()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    optimizeDeps: {
      // React 图标入口保持独立 ESM 按需加载，避免懒页面首次触达新入口时，
      // 依赖优化器提交增量结果并触发整页刷新；底层 icons-svg 是 CommonJS，仍由 Vite 转换。
      exclude: ['@ant-design/icons'],
    },
    server: {
      port: 8000,
      host: '0.0.0.0',
      strictPort: true,
      proxy: {
        [apiBasePath]: {
          target: apiProxyTarget,
          changeOrigin: true,
        },
      },
    },
    preview: {
      port: 8000,
      host: '0.0.0.0',
      strictPort: true,
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
      manifest: true,
      chunkSizeWarningLimit: 700,
      rolldownOptions: {
        input: {
          main: fileURLToPath(new URL('./index.html', import.meta.url)),
          login: fileURLToPath(new URL('./login.html', import.meta.url)),
        },
        output: {
          // 归并本来就在初始依赖链中的库，减少碎片请求；异步页面依赖不因同属组件库而提前加载。
          codeSplitting: {
            groups: [
              {
                name: 'react',
                priority: 20,
                test: /node_modules[\\/](?:react(?:-dom|-router|-router-dom|-is)?|scheduler)[\\/]/,
              },
              {
                name: 'data',
                priority: 20,
                test: /node_modules[\\/](?:@tanstack[\\/]|axios[\\/]|zustand[\\/])/,
              },
              {
                name: 'initial-ui-runtime',
                priority: 10,
                test: /node_modules[\\/](?:@rc-component|rc-[^\\/]+|@ant-design)[\\/]/,
                tags: ['$initial'],
              },
              {
                name: 'initial-ui',
                test: /node_modules[\\/]/,
                tags: ['$initial'],
              },
            ],
          },
          entryFileNames: chunkFileName,
          chunkFileNames: chunkFileName,
          assetFileNames: assetFileName,
        },
      },
    },
  };
});
