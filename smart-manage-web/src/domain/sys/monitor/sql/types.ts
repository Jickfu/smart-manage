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

export interface SqlLogListForm {
  pageNum: number;
  pageSize: number;
  keyword?: string;
  resultType?: SqlResultType;
  startTime?: string;
  endTime?: string;
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
