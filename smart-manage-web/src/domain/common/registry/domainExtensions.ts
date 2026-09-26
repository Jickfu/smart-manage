import type { ComponentType, LazyExoticComponent } from 'react';

export interface DomainViewProps {
  resourceId?: string;
  context?: Record<string, string>;
  active: boolean;
  onBack?: () => void;
  /** 容器在关闭或替换扩展视图前保护未提交内容。 */
  onDirtyChange?: (dirty: boolean) => void;
}

export interface DomainExtensions {
  views: Record<string, LazyExoticComponent<ComponentType<DomainViewProps>>>;
}

const views: DomainExtensions['views'] = {};

/** 仅装配生成器发现的领域扩展；运行时数据不能指定 import 路径。 */
export function registerDomainExtensions(modules: readonly DomainExtensions[]) {
  for (const module of modules) {
    for (const [key, component] of Object.entries(module.views)) {
      if (views[key]) throw new Error(`领域视图重复注册：${key}`);
      views[key] = component;
    }
  }
}

export function getDomainView(key: string) {
  return views[key];
}
