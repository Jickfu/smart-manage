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
  themeColor: string;
}

/** 菜单节点 */
export interface MenuVO {
  name: string;
  path: string;
  component: string;
  icon: string;
  level: number;
  routes: MenuVO[];
}
