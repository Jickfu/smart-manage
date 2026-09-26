import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import autoImport from 'unplugin-auto-import/vite';
import setupExtend from 'unplugin-vue-setup-extend-plus/vite';
import { fileURLToPath } from 'node:url';

const upstream = fileURLToPath(new URL('./target/upstream/warm-flow-vue-designer/src/', import.meta.url));
export default defineConfig({
  base: './',
  plugins: [vue(), autoImport({ imports: ['vue', 'vue-router', 'pinia'], dts: false }), setupExtend()],
  resolve: {
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
    alias: {
      '@warm-flow/designer': upstream + 'designer/index.ts',
      '@warm-flow/element-plus': upstream + 'ui/elementPlusAdapter.ts',
      '@': upstream,
    },
    dedupe: ['vue', 'pinia', 'vue-router', '@logicflow/core', '@logicflow/extension'],
  },
  build: { outDir: 'dist', chunkSizeWarningLimit: 3500 },
});
