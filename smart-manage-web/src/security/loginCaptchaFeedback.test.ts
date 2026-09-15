// @vitest-environment jsdom
import { readFileSync } from 'node:fs';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';
import { sm2 } from 'sm-crypto';

const html = readFileSync('login.html', 'utf8');
const script = html.match(/<script>([\s\S]*?)<\/script>/)![1]!;
const fetchMock = vi.fn();
const runtime = window as unknown as {
  captchaChallenge: Record<string, unknown> | null;
  captchaLoading: boolean;
  verifyCaptcha: (stopTime: number) => void;
  submitLogin: (ticket: string) => Promise<void>;
  changePassword: () => Promise<void>;
  resetPasswordByEmail: () => Promise<void>;
  passwordChangeTicket: string;
};

beforeEach(() => {
  vi.useFakeTimers();
  document.documentElement.innerHTML = html;
  // 只替代 jsdom 缺失的画布绘制，执行完整登录脚本和真实表单事件。
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue({
    clearRect: vi.fn(),
    fillRect: vi.fn(),
    strokeRect: vi.fn(),
    fillText: vi.fn(),
  } as unknown as CanvasRenderingContext2D);
  fetchMock.mockReset();
  fetchMock.mockImplementation(async () => ({ json: async () => ({ code: 1 }) }));
  vi.stubGlobal('fetch', fetchMock);
  vi.stubGlobal('sm2', sm2);
  window.eval(script);
});

afterEach(() => {
  vi.clearAllTimers();
  vi.useRealTimers();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
  document.documentElement.innerHTML = '';
});

function element<ElementType extends HTMLElement>(id: string) {
  return document.getElementById(id) as ElementType;
}

function preparePasswordForms() {
  element<HTMLInputElement>('username').value = 'test-user';
  for (const id of [
    'password',
    'newPassword',
    'confirmPassword',
    'recoveryNewPassword',
    'recoveryConfirmPassword',
  ]) {
    element<HTMLInputElement>(id).value = 'a test-only long password';
  }
  element<HTMLInputElement>('recoveryCode').value = '123456';
  runtime.passwordChangeTicket = 'test-ticket';
}

it.each(['submitLogin', 'changePassword', 'resetPasswordByEmail'] as const)(
  '%s 使用部署公钥加密，并在下次提交时读取轮换后的公钥',
  async (entry) => {
    preparePasswordForms();
    for (const keyPair of [sm2.generateKeyPairHex(), sm2.generateKeyPairHex()]) {
      fetchMock.mockClear();
      fetchMock.mockImplementation(async (url: string) => ({
        ok: true,
        json: async () =>
          url.endsWith('/password/publicKey')
            ? { code: 0, data: keyPair.publicKey }
            : { code: 1, msg: '测试响应' },
      }));
      runtime.passwordChangeTicket = 'test-ticket';
      await runtime[entry]('test-ticket');
      await vi.advanceTimersByTimeAsync(0);
      const request = fetchMock.mock.calls.find(([url]) => !url.endsWith('/password/publicKey'));
      expect(request).toBeDefined();
      const payload = JSON.parse(request![1].body);
      expect(sm2.doDecrypt(payload.password ?? payload.newPassword, keyPair.privateKey, 1)).toBe(
        'a test-only long password',
      );
      expect(fetchMock.mock.calls[0]?.[1]).toEqual({ cache: 'no-store' });
    }
  },
);

it.each(['submitLogin', 'changePassword', 'resetPasswordByEmail'] as const)(
  '%s 在公钥缺失、无效或网络失败时停止提交并恢复按钮',
  async (entry) => {
    preparePasswordForms();
    for (const response of [
      { ok: false },
      { ok: true, json: async () => ({ code: 0, data: null }) },
      { ok: true, json: async () => ({ code: 0, data: '04' + '00'.repeat(64) }) },
      null,
    ]) {
      fetchMock.mockClear();
      fetchMock.mockImplementation(async () => {
        if (!response) throw new Error('test-only network failure');
        return response;
      });
      await runtime[entry]('test-ticket');
      await vi.advanceTimersByTimeAsync(0);
      expect(fetchMock).toHaveBeenCalledTimes(1);
      expect(fetchMock.mock.calls[0]?.[0]).toContain('/password/publicKey');
      expect(element('errorMsgText').textContent).toBe('获取密码加密公钥失败，请稍后重试');
      for (const id of ['loginBtn', 'changePasswordBtn', 'resetPasswordByEmailBtn']) {
        expect(element<HTMLButtonElement>(id).disabled).toBe(false);
      }
    }
  },
);

it.each(['challenge', 'verify'] as const)('%s 限流显示在登录表单且停止自动刷新', async (stage) => {
  const message = '当前账号和网络登录失败次数过多';
  fetchMock.mockResolvedValue({ json: async () => ({ code: 100429, msg: message }) });
  element<HTMLInputElement>('username').value = 'test-user';
  element<HTMLInputElement>('password').value = 'test-only-value';
  if (stage === 'challenge') {
    element('loginForm').dispatchEvent(new Event('submit', { cancelable: true }));
  } else {
    element('captchaModal').hidden = false;
    element<HTMLButtonElement>('loginBtn').disabled = true;
    runtime.captchaChallenge = {
      challengeId: 'test',
      templateImageWidth: 50,
      backgroundImageWidth: 300,
    };
    runtime.verifyCaptcha(100);
  }
  await vi.advanceTimersByTimeAsync(0);
  expect(element('captchaModal').hidden).toBe(true);
  expect(element('errorMsg').classList.contains('is-visible')).toBe(true);
  expect(element('errorMsgText').textContent).toBe(message);
  expect(element<HTMLButtonElement>('loginBtn').disabled).toBe(false);
  expect(runtime.captchaLoading).toBe(false);
  const requests = fetchMock.mock.calls.length;
  await vi.advanceTimersByTimeAsync(2000);
  expect(fetchMock).toHaveBeenCalledTimes(requests);
});

it('拼图校验失败继续在滑块内提示并刷新挑战', async () => {
  fetchMock.mockResolvedValue({ json: async () => ({ code: 100400, msg: '拼图不匹配' }) });
  element('captchaModal').hidden = false;
  runtime.captchaChallenge = {
    challengeId: 'test',
    templateImageWidth: 50,
    backgroundImageWidth: 300,
  };
  runtime.verifyCaptcha(100);
  await vi.advanceTimersByTimeAsync(0);
  expect(element('captchaModal').hidden).toBe(false);
  expect(element('captchaStatus').textContent).toBe('拼图不匹配');
  expect(element('errorMsg').classList.contains('is-visible')).toBe(false);
  const requests = fetchMock.mock.calls.length;
  await vi.advanceTimersByTimeAsync(1000);
  expect(fetchMock).toHaveBeenCalledTimes(requests + 1);
  expect(fetchMock.mock.lastCall?.[0]).toContain('/captcha/challenge');
});
