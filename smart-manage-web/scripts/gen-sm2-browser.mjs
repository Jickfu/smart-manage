import { fileURLToPath } from 'node:url';
import path from 'node:path';
import { createRequire } from 'node:module';
import { build } from 'vite';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const require = createRequire(import.meta.url);

await build({
  configFile: false,
  logLevel: 'warn',
  publicDir: false,
  plugins: [
    {
      name: 'browser-crypto-stub',
      enforce: 'pre',
      resolveId(source) {
        return source === 'crypto' ? '\0browser-crypto-stub' : null;
      },
      load(id) {
        return id === '\0browser-crypto-stub' ? 'export default {};' : null;
      },
    },
  ],
  build: {
    emptyOutDir: false,
    lib: {
      entry: require.resolve('sm-crypto/src/sm2/index.js'),
      name: 'sm2',
      formats: ['iife'],
      fileName: () => 'sm2.js',
    },
    minify: true,
    outDir: path.resolve(projectRoot, 'public/js'),
    rollupOptions: {
      output: {
        banner: '/* 由 pnpm gen:sm2 从 sm-crypto 依赖生成，请勿手工修改。 */',
      },
    },
    sourcemap: false,
  },
});
