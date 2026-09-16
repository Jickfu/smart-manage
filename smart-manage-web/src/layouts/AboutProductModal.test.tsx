// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { afterEach, expect, it, vi } from 'vitest';
import AboutProductModal from './AboutProductModal';

const mocks = vi.hoisted(() => ({
  getBackendVersion: vi.fn(),
  frontendVersion: '1.0.0',
}));
vi.mock('@/api/productVersion', () => mocks);
vi.mock('@/domain/common/component/AppModal', () => ({
  default: ({ children, footer }: { children: ReactNode; footer: ReactNode }) => (
    <div>
      {children}
      {footer}
    </div>
  ),
}));
afterEach(() => {
  vi.unstubAllGlobals();
  vi.clearAllMocks();
});

it('shows independent versions without a copy button', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  mocks.getBackendVersion.mockResolvedValue({
    version: '2.0.0',
  });
  const container = document.createElement('div');
  const root = createRoot(container);
  const client = new QueryClient();
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <AboutProductModal systemName="测试产品" logo="/logo.svg" onClose={() => {}} />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 30));
    });
    expect(container.querySelectorAll('dd')[0]?.textContent).toBe('1.0.0');
    expect(container.querySelectorAll('dd')[1]?.textContent).toBe('2.0.0');
    expect(container.textContent).not.toContain('复制');
    expect(container.textContent).not.toContain('模块化企业管理平台');
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});

it('does not display a cached backend version when refreshing fails', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  mocks.getBackendVersion.mockRejectedValue(new Error('unavailable'));
  const container = document.createElement('div');
  const root = createRoot(container);
  const client = new QueryClient();
  client.setQueryData(['sys', 'product', 'version'], {
    version: 'old-backend',
  });
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <AboutProductModal systemName="测试产品" logo="/logo.svg" onClose={() => {}} />
        </QueryClientProvider>,
      ),
    );
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 30));
    });
    expect(container.textContent).toContain('1.0.0');
    expect(container.textContent).toContain('暂不可用');
    expect(container.textContent).not.toContain('old-backend');
  } finally {
    await act(async () => root.unmount());
    client.clear();
  }
});
