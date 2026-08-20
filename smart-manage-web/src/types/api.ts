/** 后端统一响应体 */
export interface Result<T = unknown> {
  code: number;
  msg: string;
  data: T;
  traceId: string;
}

/** 分页数据载荷，依托 Result<T> 返回 */
export interface PageData<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  records: T[];
}

/** 分页入参 */
export interface PageForm {
  pageNum: number;
  pageSize: number;
  filters?: string;
  sortField?: string;
  sortOrder?: 'ASC' | 'DESC';
}

/** ID 入参 */
export interface IdForm {
  id: string;
}

/** 用户信息 */
export interface UserInfoVO {
  id: string;
  username: string;
  name: string;
  avatar: string;
  avatarAttachmentId?: string;
  themeColor: string;
  number?: string;
  gender?: 'MALE' | 'FEMALE';
  birthday?: string;
  email?: string;
  phone?: string;
  currentOrgId: string;
  currentOrgName: string;
  companyName: string;
  assignments: UserAssignmentVO[];
}

export interface UserAssignmentVO {
  id: string;
  org: {
    id: string;
    number: string;
    name: string;
  };
  orgNamePath: string;
  position?: string;
  isOrgLeader: boolean;
  isPrimary: boolean;
}

/** 菜单节点 */
export interface MenuVO {
  id: string;
  name: string;
  path?: string;
  component?: string;
  targetType?: 'INTERNAL_PAGE' | 'EXTERNAL_LINK';
  externalUrl?: string;
  externalOpenMode?: 'NEW_TAB' | 'IFRAME';
  icon: string;
  level: number;
  routes: MenuVO[];
}
