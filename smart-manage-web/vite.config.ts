import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const apiProxyTarget = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 8000,
    host: '0.0.0.0',
    strictPort: true,
    proxy: {
      '/smart-manage-api': {
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
    chunkSizeWarningLimit: 700,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/react') || id.includes('node_modules/react-router-dom')) {
            return 'react';
          }
          if (
            id.includes('node_modules/@tanstack/') ||
            id.includes('node_modules/axios') ||
            id.includes('node_modules/zustand')
          ) {
            return 'vendors';
          }
        },
      },
    },
  },
});
