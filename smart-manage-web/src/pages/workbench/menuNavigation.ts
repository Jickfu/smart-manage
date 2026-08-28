import type { MenuVO } from '@/types/api';

export type MenuAction =
  | { type: 'INTERNAL_PAGE'; componentKey: string }
  | { type: 'EXTERNAL_NEW_TAB'; externalUrl: string }
  | { type: 'EXTERNAL_IFRAME'; menuId: string; title: string; externalUrl: string };

/** 只从服务端返回的当前用户可见菜单树中解析可打开的页面入口。 */
export function findMenuEntry(items: MenuVO[], entryNumber: string): MenuVO | null {
  for (const item of items) {
    if (item.level === 1 && item.number === entryNumber) return item;
    if (item.routes?.length) {
      const found = findMenuEntry(item.routes, entryNumber);
      if (found) return found;
    }
  }
  return null;
}

function validateExternalUrl(menuName: string, rawExternalUrl?: string) {
  const externalUrl = rawExternalUrl?.trim();
  if (!externalUrl) throw new Error(`外部链接菜单“${menuName}”缺少链接地址`);
  try {
    const url = new URL(externalUrl);
    if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password) {
      throw new Error();
    }
    return externalUrl;
  } catch {
    throw new Error(`外部链接菜单“${menuName}”的链接地址无效`);
  }
}

/** 菜单目标必须完整且自洽，禁止用名称或路径猜测缺失的页面配置。 */
export function resolveMenuAction(item: MenuVO): MenuAction {
  if (item.targetType === 'EXTERNAL_LINK') {
    const externalUrl = validateExternalUrl(item.name, item.externalUrl);
    if (item.externalOpenMode === 'NEW_TAB') {
      return { type: 'EXTERNAL_NEW_TAB', externalUrl };
    }
    if (item.externalOpenMode === 'IFRAME') {
      return { type: 'EXTERNAL_IFRAME', menuId: item.id, title: item.name, externalUrl };
    }
    throw new Error(`外部链接菜单“${item.name}”缺少有效的打开方式`);
  }

  if (item.targetType === 'INTERNAL_PAGE') {
    const componentKey = item.component?.trim();
    if (!componentKey) throw new Error(`内部页面菜单“${item.name}”缺少组件`);
    return { type: 'INTERNAL_PAGE', componentKey };
  }

  throw new Error(`菜单“${item.name}”缺少有效的页面目标类型`);
}
