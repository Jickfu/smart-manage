// @vitest-environment jsdom
import { act, StrictMode, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider, useQuery, useQueryClient } from '@tanstack/react-query';
import { afterEach, expect, it, vi } from 'vitest';
import { QueryFeedbackProvider } from './QueryFeedbackProvider';
import { usePermissionAccess } from '../page/access/usePermissionAccess';
import { ApiError } from '@/api/ApiError';

const mocks = vi.hoisted(() => ({
  feedback: { fromError: vi.fn(), close: vi.fn() },
  getCurrentPermissions: vi.fn(),
}));
vi.mock('./useOperationFeedback', () => ({ useOperationFeedback: () => mocks.feedback }));
vi.mock('@/api/user', () => ({ getCurrentPermissions: mocks.getCurrentPermissions }));
afterEach(() => {
  vi.unstubAllGlobals();
  vi.clearAllMocks();
});

it('keeps an actual active query alive across StrictMode effect replay and clears on unmount', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  let client!: QueryClient;
  let resolveQuery!: (data: string) => void;
  const queryFn = vi.fn(
    () =>
      new Promise<string>((resolve) => {
        resolveQuery = resolve;
      }),
  );
  const effectSetups = vi.fn();
  const effectCleanups = vi.fn();
  function Probe() {
    client = useQueryClient();
    const query = useQuery({ queryKey: ['strict-live'], queryFn, initialData: 'cached' });
    useEffect(() => {
      effectSetups();
      return effectCleanups;
    }, []);
    return <span>{query.data}</span>;
  }
  await act(async () =>
    root.render(
      <StrictMode>
        <QueryFeedbackProvider>
          <Probe />
        </QueryFeedbackProvider>
      </StrictMode>,
    ),
  );
  expect(effectSetups).toHaveBeenCalledTimes(2);
  expect(effectCleanups).toHaveBeenCalledTimes(1);
  expect(queryFn).toHaveBeenCalledTimes(1);
  expect(client.getQueryData(['strict-live'])).toBe('cached');
  expect(client.isFetching()).toBe(1);
  await act(async () => {
    resolveQuery('fresh');
  });
  // Query 的批量 observer 通知在下一任务交付，先结算请求再等待 UI 通知。
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
  expect(client.getQueryData(['strict-live'])).toBe('fresh');
  expect(container.textContent).toBe('fresh');
  await act(async () => root.unmount());
  expect(client.getQueryCache().getAll()).toHaveLength(0);
  expect(mocks.feedback.fromError).not.toHaveBeenCalled();
});

it('revokes cached permissions for any failed permission query and restores them after success', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const queryKey = ['current-user-permissions', 'test'];
  client.setQueryData(queryKey, ['test:save']);
  function Probe() {
    const access = usePermissionAccess('test');
    return <button disabled={!access.can('test:save')}>保存</button>;
  }
  await act(async () =>
    root.render(
      <QueryClientProvider client={client}>
        <Probe />
      </QueryClientProvider>,
    ),
  );
  expect(container.querySelector('button')?.disabled).toBe(false);
  mocks.getCurrentPermissions.mockRejectedValue(new ApiError({ source: 'NETWORK', message: '' }));
  await act(async () => {
    await client.refetchQueries({ queryKey });
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
  expect(client.getQueryData(queryKey)).toEqual(['test:save']);
  expect(container.querySelector('button')?.disabled).toBe(true);
  mocks.getCurrentPermissions.mockResolvedValue(['test:save']);
  await act(async () => {
    await client.refetchQueries({ queryKey });
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
  expect(container.querySelector('button')?.disabled).toBe(false);
  await act(async () => root.unmount());
  client.clear();
});
