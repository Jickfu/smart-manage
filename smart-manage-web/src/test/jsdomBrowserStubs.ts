import { vi } from 'vitest';

// 仅由需要布局替身的 jsdom 测试显式调用；清理由调用方的 restoreAllMocks/unstubAllGlobals 完成。
export function installJsdomBrowserStubs() {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  // jsdom 不提供尺寸观察器，不在业务交互测试中模拟尺寸变化。
  vi.stubGlobal(
    'ResizeObserver',
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  // 忽略 jsdom 不支持的伪元素参数，保留真实元素样式计算。
  const getComputedStyle = window.getComputedStyle.bind(window);
  vi.spyOn(window, 'getComputedStyle').mockImplementation((element) => getComputedStyle(element));
  vi.stubGlobal('matchMedia', () => ({
    matches: false,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
  }));
}
