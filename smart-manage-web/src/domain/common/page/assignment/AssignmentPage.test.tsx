// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { expect, it, vi } from 'vitest';
import { AssignmentPage } from './AssignmentPage';
import { ApiError } from '@/api/ApiError';

const mocks = vi.hoisted(() => ({ dirty: false }));

vi.mock('../tab/useBeforeCloseGuard', () => ({
  useBeforeCloseGuard: (_app: string, _tab: string, dirty: boolean) => {
    mocks.dirty = dirty;
  },
}));

it('保留分配内容、上下文和阻断后的保存边界', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  const queryClient = new QueryClient();
  const onSave = vi.fn();
  const renderAssignment = async (error?: Error, showHeaderContext?: boolean) =>
    act(async () =>
      root.render(
        <QueryClientProvider client={queryClient}>
          <AssignmentPage
            loading={false}
            saving={false}
            dirty
            subject="角色摘要"
            selectedCount={1}
            totalCount={2}
            showHeaderContext={showHeaderContext}
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
    expect(container.querySelector('.sm-assignment-header-context')?.textContent).toContain(
      '角色摘要',
    );
    const input = container.querySelector('input');
    await renderAssignment(new ApiError({ source: 'API', message: '无权访问', apiCode: 100403 }));
    expect(container.querySelector('input')).toBe(input);
    expect(input?.closest('[hidden][inert]')).not.toBeNull();
    const button = [...container.querySelectorAll('button')].find(
      (candidate) => candidate.textContent?.replace(/\s/g, '') === '保存',
    )!;
    await act(async () => button.click());
    expect(onSave).not.toHaveBeenCalled();
    await renderAssignment(undefined, false);
    expect(container.querySelector('input')).toBe(input);
    expect(container.querySelector('.sm-assignment-header-context')).toBeNull();
    expect(mocks.dirty).toBe(true);
  } finally {
    await act(async () => root.unmount());
    queryClient.clear();
    vi.unstubAllGlobals();
  }
});
