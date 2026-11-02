// @vitest-environment jsdom
import { act, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import PersonalCredentialModal from './PersonalCredentialModal';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import request from '@/api/request';

const mocks = vi.hoisted(() => ({
  feedback: { success: vi.fn(), fromError: vi.fn() },
  getCurrentPasswordPublicKey: vi.fn(),
  verifyCurrentUserPassword: vi.fn(),
  doEncrypt: vi.fn(),
}));

vi.mock('@/domain/common/component/useOperationFeedback', () => ({
  useOperationFeedback: () => mocks.feedback,
}));
vi.mock('@/domain/common/component/AppModal', () => ({
  default: ({ children }: { children: ReactNode }) => <div>{children}</div>,
}));
vi.mock('sm-crypto', () => ({ sm2: { doEncrypt: mocks.doEncrypt } }));
vi.mock('@/api/user', () => ({
  getCurrentPasswordPublicKey: mocks.getCurrentPasswordPublicKey,
  verifyCurrentUserPassword: mocks.verifyCurrentUserPassword,
  updateCurrentUserContact: vi.fn(),
  updateCurrentUserPassword: vi.fn(),
  requestCurrentPasswordEmailCode: vi.fn(),
  updateCurrentUserPasswordByEmail: vi.fn(),
  requestCurrentEmailCode: vi.fn(),
  bindCurrentEmail: vi.fn(),
}));

beforeEach(() => {
  vi.spyOn(request, 'get').mockResolvedValue({
    data: { data: { minLength: 15, maxLength: 64, complexityEnabled: false } },
  });
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  vi.stubGlobal('matchMedia', () => ({
    matches: false,
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
  mocks.getCurrentPasswordPublicKey.mockResolvedValue('public-key');
  mocks.doEncrypt.mockReturnValue('encrypted-password');
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
  vi.clearAllMocks();
  document.body.innerHTML = '';
});

async function renderPasswordModal() {
  const container = document.createElement('div');
  document.body.append(container);
  const root = createRoot(container);
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  queryClient.setQueryData(['password-policy'], {
    minLength: 15,
    maxLength: 64,
    complexityEnabled: false,
  });
  queryClient.setDefaultOptions({ queries: { retry: false, staleTime: Infinity } });
  await act(async () => {
    root.render(
      <QueryClientProvider client={queryClient}>
        <PersonalCredentialModal
          type="PASSWORD"
          onClose={() => {}}
          onProfileSaved={() => {}}
          onPasswordChanged={() => {}}
          emailPasswordAvailable={false}
        />
      </QueryClientProvider>,
    );
  });
  const passwordInput = container.querySelector('input[type="password"]') as HTMLInputElement;
  const valueSetter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value')?.set;
  await act(async () => {
    valueSetter?.call(passwordInput, 'current-password');
    passwordInput.dispatchEvent(new Event('input', { bubbles: true }));
  });
  return { container, root };
}

async function clickNext(container: HTMLElement) {
  const nextButton = [...container.querySelectorAll('button')].find(
    (button) => button.textContent?.trim() === '下一步',
  );
  await act(async () => {
    nextButton?.dispatchEvent(new MouseEvent('click', { bubbles: true }));
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
}

it('verifies the original password before entering the password change step', async () => {
  mocks.verifyCurrentUserPassword.mockResolvedValue(undefined);
  const { container, root } = await renderPasswordModal();
  try {
    await clickNext(container);

    expect(mocks.getCurrentPasswordPublicKey).toHaveBeenCalledTimes(1);
    expect(mocks.doEncrypt).toHaveBeenCalledWith('current-password', 'public-key', 1);
    expect(mocks.verifyCurrentUserPassword).toHaveBeenCalledWith('encrypted-password');
    expect(container.querySelectorAll('form')[1]?.hidden).toBe(false);
  } finally {
    await act(async () => root.unmount());
  }
});

it('keeps the verification step and displays the server error when the password is wrong', async () => {
  const error = new Error('原密码错误');
  mocks.verifyCurrentUserPassword.mockRejectedValue(error);
  const { container, root } = await renderPasswordModal();
  try {
    await clickNext(container);

    expect(container.querySelectorAll('form')[0]?.hidden).toBe(false);
    expect(container.querySelectorAll('form')[1]?.hidden).toBe(true);
    expect(mocks.feedback.fromError).toHaveBeenCalledWith(error, '验证失败');
  } finally {
    await act(async () => root.unmount());
  }
});
