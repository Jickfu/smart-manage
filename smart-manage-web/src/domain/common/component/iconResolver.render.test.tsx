// @vitest-environment jsdom
import { act } from 'react';
import { createRoot } from 'react-dom/client';
import { beforeAll, expect, it, vi } from 'vitest';
import DynamicIcon from './DynamicIcon';
import generatedCatalog from './iconCatalog.generated.json';
import { initializeIconCatalog } from './iconCatalog';
import { resolveIcon } from './iconResolver';

beforeAll(async () => {
  await initializeIconCatalog(generatedCatalog);
});

it('任意合法名称首次渲染立即出现 SVG，不经历请求或加载回退', () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    act(() => root.render(resolveIcon('RadarChartOutlined')));
    expect(container.querySelector('[data-icon="radar-chart"]')).not.toBeNull();
    expect(container.querySelector('[aria-label="图标加载中"]')).toBeNull();

    act(() => root.render(resolveIcon('UnknownOutlined', '默认图标')));
    expect(container.textContent).toBe('默认图标');
  } finally {
    act(() => root.unmount());
    vi.unstubAllGlobals();
  }
});

it('TwoTone 实例分别使用主辅色且互不污染', () => {
  vi.stubGlobal('IS_REACT_ACT_ENVIRONMENT', true);
  const container = document.createElement('div');
  const root = createRoot(container);
  try {
    act(() =>
      root.render(
        <>
          <DynamicIcon name="RocketTwoTone" twoToneColor={['#112233', '#445566']} />
          <DynamicIcon name="RocketTwoTone" twoToneColor={['#aabbcc', '#ddeeff']} />
        </>,
      ),
    );
    const icons = [...container.querySelectorAll('[data-icon="rocket"]')];
    expect(icons[0]?.querySelector('[fill="#112233"]')).not.toBeNull();
    expect(icons[0]?.querySelector('[fill="#445566"]')).not.toBeNull();
    expect(icons[1]?.querySelector('[fill="#aabbcc"]')).not.toBeNull();
    expect(icons[1]?.querySelector('[fill="#ddeeff"]')).not.toBeNull();
  } finally {
    act(() => root.unmount());
    vi.unstubAllGlobals();
  }
});

it('全集初始化本身不创建任何 SVG DOM', () => {
  expect(document.querySelectorAll('svg, symbol, path')).toHaveLength(0);
});
