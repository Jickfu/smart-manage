// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { expect, it, vi } from 'vitest';
import { loadIcon } from './iconCatalog';
import { resolveIcon } from './iconResolver';

it('按名称异步显示白名单之外的图标，切换名称不残留旧图标', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await act(async () => root.render(resolveIcon('RadarChartOutlined')));
    await vi.waitFor(async () => {
      await act(async () => {});
      expect(container.querySelector('[aria-label="radar-chart"] svg')).not.toBeNull();
    });
    await act(async () => root.render(resolveIcon('UnknownOutlined', '默认图标')));
    expect(container.textContent).toBe('默认图标');
    expect(container.querySelector('svg')).toBeNull();
    await act(async () => root.render(resolveIcon('RadarChartOutlined')));
    expect(container.querySelector('[aria-label="radar-chart"] svg')).not.toBeNull();
  } finally {
    await act(async () => root.unmount());
    vi.unstubAllGlobals();
  }
});

it('预加载完成后首次渲染立即出现 SVG，不经历异步回退', async () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    await loadIcon('RocketTwoTone');
    act(() => root.render(resolveIcon('RocketTwoTone')));
    expect(container.querySelector('[aria-label="rocket"] svg')).not.toBeNull();
    expect(container.querySelector('[aria-label="图标加载中"]')).toBeNull();
  } finally {
    act(() => root.unmount());
    vi.unstubAllGlobals();
  }
});
