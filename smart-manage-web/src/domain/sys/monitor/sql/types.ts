export type SqlResultType = 'QUERY' | 'DML' | 'DDL' | 'ERROR';

export interface SqlColumn {
  key: string;
  label: string;
  typeName: string;
  comment: string;
}

export interface SqlExecutionResult {
  type: SqlResultType;
  columns?: SqlColumn[];
  rows?: unknown[][];
  rowCount: number;
  executeDuration: number;
  message?: string;
  truncated: boolean;
  statementCount: number;
  statementRowCounts?: number[];
}

export type SqlCatalogObjectType =
  | 'TABLE'
  | 'PARTITIONED_TABLE'
  | 'VIEW'
  | 'MATERIALIZED_VIEW'
  | 'FOREIGN_TABLE';

export interface SqlCatalogObject {
  schemaName: string;
  objectName: string;
  objectType: SqlCatalogObjectType;
  comment?: string;
}

export interface SqlCatalogColumn {
  position: number;
  columnName: string;
  dataType: string;
  nullable: boolean;
  primaryKey: boolean;
  defaultValue?: string;
  comment?: string;
}

export interface SqlCatalogResult {
  objects: SqlCatalogObject[];
  truncated: boolean;
}

export interface SqlCatalogDetail extends SqlCatalogObject {
  columns: SqlCatalogColumn[];
}

export interface SqlLogListForm {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  resultType?: SqlResultType;
  startTime?: string;
  endTime?: string;
  filters?: string;
  sortField?: string;
  sortOrder?: 'ASC' | 'DESC';
}

export interface SqlLogListItem {
  id: string;
  sqlText: string;
  executeDuration: number;
  resultType: SqlResultType;
  rowCount: number;
  createName?: string;
  createIp?: string;
  createTime: string;
}

export interface SqlLogDetail extends SqlLogListItem {
  errorMessage?: string;
  remark?: string;
}
