import { createContext } from 'react';
import type { PageType } from '../types';

export interface PageTabTitleContextValue {
  pageType: PageType;
  setTabTitle: (title: string) => void;
}

export const PageTabTitleContext = createContext<PageTabTitleContextValue | null>(null);
