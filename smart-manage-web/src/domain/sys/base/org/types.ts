export type OrgType = 'GROUP' | 'COMPANY' | 'DEPARTMENT';

export interface OrgTreeNode {
  id: string;
  number: string;
  name: string;
  orgType: OrgType;
  enabled: boolean;
  archived: boolean;
  children: OrgTreeNode[];
}

export interface OrgListVO {
  id: string;
  number: string;
  name: string;
  parentId?: string;
  numberPath: string;
  namePath: string;
  orgType: OrgType;
  sort: number;
  enabled: boolean;
  archived: boolean;
  archivedAt?: string;
  description?: string;
  version: number;
}

export type OrgDetailVO = OrgListVO;

export interface OrgListForm {
  pageNum: number;
  pageSize: number;
  parentId?: string;
  includeDescendants?: boolean;
  showArchived?: boolean;
  keyword?: string;
}

export interface OrgParentListForm extends OrgListForm {
  excludedId?: string;
}

export interface OrgSaveForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  parentId?: string;
  orgType: OrgType;
  sort: number;
  description?: string;
}
