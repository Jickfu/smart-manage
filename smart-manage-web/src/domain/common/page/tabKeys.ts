const TAB_KEY_SEPARATOR = ':';

/** 列表页 tab key：list:componentKey */
export function createListTabKey(componentKey: string) {
  return ['list', componentKey].join(TAB_KEY_SEPARATOR);
}

/** 单据页 tab key：bill:componentKey:billId */
export function createBillTabKey(componentKey: string, billId: string) {
  return ['bill', componentKey, billId].join(TAB_KEY_SEPARATOR);
}

/** 新增页 tab key：addnew:componentKey:uuid（每次新增独立 key） */
export function createAddNewTabKey(componentKey: string) {
  return ['addnew', componentKey, crypto.randomUUID()].join(TAB_KEY_SEPARATOR);
}

/** 外部链接 iframe 页签：同一菜单只保留一个工作台实例。 */
export function createExternalLinkTabKey(menuId: string) {
  return ['external', menuId].join(TAB_KEY_SEPARATOR);
}
