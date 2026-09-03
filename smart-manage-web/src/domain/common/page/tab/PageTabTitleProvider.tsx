import { useMemo } from 'react';
import type { ReactNode } from 'react';
import type { PageType } from '../types';
import { PageTabTitleContext } from './pageTabTitleContext';

interface PageTabTitleProviderProps {
  pageType: PageType;
  setTabTitle: (title: string) => void;
  children: ReactNode;
}

/** 为已注册页面提供受控的页签标题更新能力。 */
export function PageTabTitleProvider({
  pageType,
  setTabTitle,
  children,
}: PageTabTitleProviderProps) {
  const value = useMemo(() => ({ pageType, setTabTitle }), [pageType, setTabTitle]);
  return <PageTabTitleContext.Provider value={value}>{children}</PageTabTitleContext.Provider>;
}
