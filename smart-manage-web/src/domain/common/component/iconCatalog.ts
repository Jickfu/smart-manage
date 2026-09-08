import { createElement, type ComponentType, type ReactElement } from 'react';

// 构建只枚举独立入口，搜索目录不会执行 import 或加载 SVG。
const iconModules = import.meta.glob<{ default: ComponentType }>(
  '/node_modules/@ant-design/icons/es/icons/*{Outlined,Filled,TwoTone}.js',
);
const iconLoaders = new Map(
  Object.entries(iconModules).map(([path, loader]) => [
    path.slice(path.lastIndexOf('/') + 1, -3),
    loader,
  ]),
);

export const selectableIconNames: readonly string[] = [...iconLoaders.keys()].sort();

export function isSelectableIconName(name: string): boolean {
  return iconLoaders.has(name);
}

type IconSnapshot = ReactElement | null | undefined;
const loadedIcons = new Map<string, IconSnapshot>();
const requests = new Map<string, Promise<void>>();
const listeners = new Map<string, Set<() => void>>();

/** undefined 表示尚未加载，null 表示加载失败；快照稳定，已加载组件可同步渲染。 */
export function getIconSnapshot(name: string): IconSnapshot {
  return loadedIcons.get(name);
}

export function subscribeIcon(name: string, listener: () => void): () => void {
  const subscribers = listeners.get(name) ?? new Set();
  subscribers.add(listener);
  listeners.set(name, subscribers);
  return () => {
    subscribers.delete(listener);
    if (subscribers.size === 0) listeners.delete(name);
  };
}

/** 同名请求去重，完成后直接通知图标，不经过 Suspense 的回退显示周期。 */
export function loadIcon(name: string): Promise<void> {
  const existing = requests.get(name);
  if (existing) return existing;
  const loader = iconLoaders.get(name);
  if (!loader) return Promise.resolve();
  const request = loader()
    .then((module) => {
      loadedIcons.set(name, createElement(module.default));
    })
    .catch(() => {
      loadedIcons.set(name, null);
    })
    .then(() => {
      listeners.get(name)?.forEach((listener) => listener());
    });
  requests.set(name, request);
  return request;
}

/** 数据展示前并行准备本次可见图标；最多等待 150ms，慢资源不能阻塞菜单和卡片。 */
export async function preloadIcons(names: readonly (string | undefined)[]): Promise<void> {
  const pendingNames = [...new Set(names)].filter((name): name is string =>
    Boolean(name && iconLoaders.has(name) && !loadedIcons.has(name)),
  );
  if (pendingNames.length === 0) return;
  let timeout: ReturnType<typeof setTimeout> | undefined;
  try {
    await Promise.race([
      Promise.all(pendingNames.map(loadIcon)),
      new Promise<void>((resolve) => {
        timeout = setTimeout(resolve, 150);
      }),
    ]);
  } finally {
    clearTimeout(timeout);
  }
}
