// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { AssignmentPage } from './AssignmentPage';
import { ApiError } from '@/api/ApiError';

vi.mock('../tab/useBeforeCloseGuard', () => ({ useBeforeCloseGuard: () => undefined }));

it('keeps assignment content mounted and blocks save after denial', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  const queryClient = new QueryClient();
  const onSave = vi.fn();
  const renderAssignment = async (error?: Error) =>
    act(async () =>
      root.render(
        <QueryClientProvider client={queryClient}>
          <AssignmentPage
            loading={false}
            saving={false}
            dirty
            onSave={onSave}
            onExit={() => undefined}
            onRetry={() => undefined}
            access={{ prefix: '', permissions: { save: '' } }}
            error={error}
          >
            <input defaultValue="选择未保存" />
          </AssignmentPage>
        </QueryClientProvider>,
      ),
    );
  try {
    await renderAssignment();
    const input = container.querySelector('input');
    await renderAssignment(new ApiError({ source: 'API', message: '无权访问', apiCode: 100403 }));
    expect(container.querySelector('input')).toBe(input);
    expect(input?.closest('[hidden][inert]')).not.toBeNull();
    const button = [...container.querySelectorAll('button')].find(
      (candidate) => candidate.textContent?.replace(/\s/g, '') === '保存',
    )!;
    await act(async () => button.click());
    expect(onSave).not.toHaveBeenCalled();
    await renderAssignment();
    expect(container.querySelector('input')).toBe(input);
  } finally {
    await act(async () => root.unmount());
    queryClient.clear();
    vi.unstubAllGlobals();
  }
});
