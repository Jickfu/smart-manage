import type { ComponentType, LazyExoticComponent } from 'react';

/** 页面类型 */
export type PageType = 'LIST' | 'EDIT' | 'CUSTOM';

/** 操作类型 */
export enum OperationType {
  ADDNEW = 'ADDNEW',
  EDIT = 'EDIT',
  VIEW = 'VIEW',
}

/** 单据状态 */
export enum BillStatus {
  /** 暂存 */
  SAVED = 'A',
  /** 已提交 */
  SUBMITTED = 'B',
  /** 审核通过 */
  AUDITED = 'C',
  /** 已关闭 */
  CLOSED = 'D',
}

/** 页面组件统一 Props */
export interface PageComponentProps {
  appNumber: string;
  componentKey: string;
  tabKey: string;
  title: string;
  operationType?: OperationType;
  billId?: string;
  context?: Record<string, string>;
  temporary?: boolean;
  /** 当前页签是否激活 — 非激活页面应暂停轮询/昂贵计算 */
  active: boolean;
}

/** 页面注册项 */
export interface PageRegistration {
  componentKey: string;
  pageType: PageType;
  component:
    | ComponentType<PageComponentProps>
    | LazyExoticComponent<ComponentType<PageComponentProps>>;
}
