import type { ComponentType, LazyExoticComponent } from 'react';
import type { PageComponentProps, PageType } from '@/domain/common/page/types';

export interface PageRegistration {
  componentKey: string;
  title: string;
  pageType: PageType;
  component:
    | ComponentType<PageComponentProps>
    | LazyExoticComponent<ComponentType<PageComponentProps>>;
}

/** 页面组件白名单，由各业务模块的 pageRegistration.ts 显式声明。 */
export const componentRegistry: Record<string, Omit<PageRegistration, 'componentKey'>> = {};

/** 根据页面注册信息生成稳定的默认页签标题，菜单名称不得参与该过程。 */
export function getRegisteredTabTitle(componentKey: string, expectedPageType?: PageType): string {
  const registration = componentRegistry[componentKey];
  if (!registration) {
    throw new Error(`[registry] 页面组件 "${componentKey}" 未注册。`);
  }
  if (expectedPageType && registration.pageType !== expectedPageType) {
    throw new Error(
      `[registry] 页面 "${componentKey}" 注册为 ${registration.pageType}，不能按 ${expectedPageType} 打开。`,
    );
  }
  return registration.pageType === 'LIST' ? `${registration.title}列表` : registration.title;
}

/**
 * 声明一个业务模块包含的全部页面。
 * 页面组件保持一文件一组件，pageRegistration.ts 只承担模块级清单职责。
 */
export function definePageRegistrations(
  registrations: readonly PageRegistration[],
): readonly PageRegistration[] {
  if (registrations.length === 0) {
    throw new Error('[registry] 页面注册清单不能为空。');
  }
  const moduleKeys = new Set<string>();
  for (const { componentKey, title } of registrations) {
    if (moduleKeys.has(componentKey)) {
      throw new Error(`[registry] 模块清单内的 componentKey "${componentKey}" 重复。`);
    }
    if (!title.trim()) {
      throw new Error(`[registry] 页面 "${componentKey}" 的 title 不能为空。`);
    }
    moduleKeys.add(componentKey);
  }
  return registrations;
}

/** 汇总所有模块清单并写入运行时白名单。 */
export function registerPageRegistrationModules(
  modules: readonly (readonly PageRegistration[])[],
): void {
  if (modules.length === 0) {
    throw new Error('[registry] 未发现页面注册模块。');
  }
  for (const registrations of modules) {
    for (const { componentKey, title, pageType, component } of registrations) {
      if (componentRegistry[componentKey]) {
        throw new Error(`[registry] componentKey "${componentKey}" 重复注册。`);
      }
      componentRegistry[componentKey] = { title: title.trim(), pageType, component };
    }
  }
}
