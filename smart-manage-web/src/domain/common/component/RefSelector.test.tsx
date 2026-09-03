// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { Button } from 'antd';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import RefSelector from './RefSelector';
import { ApiError } from '@/api/ApiError';

it('renders the actual local request error and retries without falling into ErrorBoundary', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  // jsdom 没有布局观察器；本测试关注真实错误分支与请求重试，不模拟尺寸变化。
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  const getComputedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => getComputedStyle(element));
  Object.defineProperty(window, 'matchMedia', {
    configurable: true,
    value: () => ({
      matches: false,
      addListener: () => undefined,
      removeListener: () => undefined,
    }),
  });
  const container = document.createElement('div');
  document.body.append(container);
  const root = createRoot(container);
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const fetchFn = vi
    .fn()
    .mockRejectedValueOnce(
      new ApiError({
        source: 'NETWORK',
        message: 'private socket detail',
        traceId: 'selector-test-01',
      }),
    )
    .mockResolvedValue({ records: [], total: 0 });
  try {
    await act(async () =>
      root.render(
        <QueryClientProvider client={client}>
          <RefSelector
            selectorKey="error-render-test"
            fetchFn={fetchFn}
            displayRender={(record) => String(record.name)}
            fieldNames={{ key: 'id', label: 'name' }}
            columns={[{ title: '名称', dataIndex: 'name' }]}
            modalTitle="选择记录"
            trigger={<Button>打开选择器</Button>}
          />
        </QueryClientProvider>,
      ),
    );
    await act(async () => container.querySelector('button')!.click());
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(document.body.textContent).toContain('未能连接到服务器');
    expect(document.body.textContent).toContain('selector-test-01');
    expect(document.body.textContent).not.toContain('private socket detail');
    const retry = [...document.querySelectorAll('button')].find(
      (button) => button.textContent?.replace(/\s/g, '') === '重试',
    )!;
    await act(async () => retry.click());
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    expect(fetchFn).toHaveBeenCalledTimes(2);
    expect(document.body.textContent).not.toContain('未能连接到服务器');
  } finally {
    await act(async () => root.unmount());
    client.clear();
    container.remove();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  }
});
