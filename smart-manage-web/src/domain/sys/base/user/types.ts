import type { PageForm } from '@/types/api';
import type { ReferenceVO } from '@/domain/sys/base/common/types';

/** 用户列表查询 */
export interface UserListForm extends PageForm {
  keyword?: string;
  orgId?: string;
  includeDescendants?: boolean;
  unassigned?: boolean;
}

export type Gender = 'MALE' | 'FEMALE';

export interface UserAssignmentVO {
  id?: string;
  org: ReferenceVO;
  orgNamePath?: string;
  position: string;
  isOrgLeader: boolean;
  isPrimary: boolean;
}

/** 用户列表项 */
export interface UserListVO {
  id: string;
  username: string;
  name: string;
  number: string;
  avatar: string;
  avatarAttachmentId?: string;
  enabled: boolean;
  createTime?: string;
  assignments: UserAssignmentVO[];
}

/** 用户详情 — 所有 ID 均为字符串 */
export interface UserDetailVO {
  id: string;
  username: string;
  name: string;
  number: string;
  gender?: Gender;
  birthday?: string;
  avatar: string;
  avatarAttachmentId?: string;
  themeColor: string;
  email?: string;
  phone?: string;
  enabled?: boolean;
  createTime?: string;
  updateTime?: string;
  version: number;
  assignments: UserAssignmentVO[];
}

/** 用户保存 — ID 均以字符串传递 */
export interface UserSaveForm {
  id?: string;
  version?: number;
  username: string;
  password?: string;
  name: string;
  number: string;
  gender?: Gender;
  birthday?: string;
  email?: string;
  phone?: string;
  avatarAttachmentId?: string;
  attachmentUploadSessions?: Record<string, string>;
  assignments: Array<Omit<UserAssignmentVO, 'org'> & { orgId: string }>;
}

export interface ResetPasswordVO {
  password: string;
}

export interface TemporaryLoginGrantVO {
  credential: string;
  expiresAt: string;
}
