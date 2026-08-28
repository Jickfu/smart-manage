export interface StartupNavigationTarget {
  appNumber: string;
  entryNumber?: string;
}

/** 浏览器 URL 只在应用首次启动时解析，工作台内部导航仍由内存状态负责。 */
export function parseStartupNavigation(search: string): StartupNavigationTarget {
  const searchParams = new URLSearchParams(search);
  const appNumber = searchParams.get('app')?.trim() || 'home';
  const entryNumber = searchParams.get('entry')?.trim();
  return { appNumber, entryNumber: entryNumber || undefined };
}
