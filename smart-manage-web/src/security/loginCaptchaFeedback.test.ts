// @vitest-environment jsdom
import { readFileSync } from 'node:fs';
import { afterEach, beforeEach, expect, it, vi } from 'vitest';

const html = readFileSync('login.html', 'utf8');
const script = html.match(/<script>([\s\S]*?)<\/script>/)![1]!;
const fetchMock = vi.fn();
const runtime = window as unknown as {
  captchaChallenge: Record<string, unknown> | null;
  captchaLoading: boolean;
  verifyCaptcha: (stopTime: number) => void;
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
