import { expect, it, vi } from 'vitest';
import { getIconSnapshot, loadIcon, preloadIcons } from './iconCatalog';

const pendingModule = vi.hoisted(() => {
  let finish!: () => void;
  const promise = new Promise<void>((resolve) => {
    finish = resolve;
  });
  return { promise, finish };
});
vi.mock('/node_modules/@ant-design/icons/es/icons/AlertTwoTone.js', async () => {
  await pendingModule.promise;
  return { default: () => null };
});
vi.mock('/node_modules/@ant-design/icons/es/icons/CrownTwoTone.js', () => {
  throw new Error('模拟图标资源下载失败');
});

it('去重同名请求并同步保留加载完成的组件', async () => {
  const firstRequest = loadIcon('RadarChartOutlined');
  expect(loadIcon('RadarChartOutlined')).toBe(firstRequest);
  await firstRequest;
  const component = getIconSnapshot('RadarChartOutlined');
  expect(component).toBeTruthy();
  await preloadIcons(['RadarChartOutlined', 'RadarChartOutlined', undefined, 'UnknownOutlined']);
  expect(getIconSnapshot('RadarChartOutlined')).toBe(component);
});

it('慢图标最多等待 150ms，后台完成后仍可复用', async () => {
  vi.useFakeTimers();
  try {
    const preload = preloadIcons(['AlertTwoTone']);
    await vi.advanceTimersByTimeAsync(150);
    await preload;
    expect(getIconSnapshot('AlertTwoTone')).toBeUndefined();
    pendingModule.finish();
    await loadIcon('AlertTwoTone');
    expect(getIconSnapshot('AlertTwoTone')).toBeTruthy();
  } finally {
    vi.useRealTimers();
  }
});

it('图标资源失败不使业务数据预加载失败', async () => {
  await expect(preloadIcons(['CrownTwoTone'])).resolves.toBeUndefined();
  await loadIcon('CrownTwoTone');
  expect(getIconSnapshot('CrownTwoTone')).toBeNull();
});
