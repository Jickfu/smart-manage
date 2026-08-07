import { useCallback, useContext, useEffect } from 'react';
import { PageTabTitleContext } from './pageTabTitleContext';

/** 通用页面壳用基础标题同步页签；LIST 自动追加“列表”。 */
export function usePageTabTitle(title: string): void {
  const context = useContext(PageTabTitleContext);
  useEffect(() => {
    if (!context) return;
    const normalizedTitle = title.trim();
    if (!normalizedTitle) {
      throw new Error('[workbench] 页签标题不能为空。');
    }
    context.setTabTitle(context.pageType === 'LIST' ? `${normalizedTitle}列表` : normalizedTitle);
  }, [context, title]);
}

/** 页面按业务逻辑强制覆盖当前页签标题；只改变显示名称，不改变页签身份。 */
export function useSetPageTabTitle(): (title: string) => void {
  const context = useContext(PageTabTitleContext);
  return useCallback(
    (title: string) => {
      if (!context) {
        throw new Error('[workbench] 当前组件不在工作台页面上下文中。');
      }
      const normalizedTitle = title.trim();
      if (!normalizedTitle) {
        throw new Error('[workbench] 页签标题不能为空。');
      }
      context.setTabTitle(normalizedTitle);
    },
    [context],
  );
}
