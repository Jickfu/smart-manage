import type { PageForm } from '@/types/api';

export type ScriptStatus = 'SUCCESS' | 'ERROR' | 'TIMEOUT';
export type ScriptTransactionMode = 'ATOMIC' | 'NON_ATOMIC';
export type ScriptTransactionResult = 'COMMITTED' | 'ROLLED_BACK' | 'NOT_APPLICABLE';

export interface ScriptExecutionForm {
  scriptId?: string;
  content: string;
  transactionMode: ScriptTransactionMode;
}

export interface ScriptExecutionResult {
  status: ScriptStatus;
  output: string;
  errorMessage?: string;
  executeDuration: number;
  truncated: boolean;
  transactionResult: ScriptTransactionResult;
}

export interface ScriptListForm extends PageForm {
  keyword?: string;
}

export interface ScriptListItem {
  id: string;
  version: number;
  number: string;
  name: string;
  remark?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ScriptDetail extends ScriptListItem {
  version: number;
  content: string;
}

export interface ScriptSaveForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  content: string;
  remark?: string;
}

export interface ScriptLogListForm extends PageForm {
  keyword?: string;
  status?: ScriptStatus;
  transactionMode?: ScriptTransactionMode;
  startTime?: string;
  endTime?: string;
}

export interface ScriptLogListItem {
  id: string;
  scriptId?: string;
  scriptName?: string;
  transactionMode: ScriptTransactionMode;
  executeStatus: ScriptStatus;
  executeDuration: number;
  transactionResult: ScriptTransactionResult;
  createName?: string;
  createIp?: string;
  createTime: string;
}

export interface ScriptLogDetail extends ScriptLogListItem {
  scriptContent: string;
  output?: string;
  errorMessage?: string;
}

export interface ScriptApiField {
  name: string;
  type: string;
  required: boolean;
  constraints: string[];
}

export interface ScriptApiParameter {
  name: string;
  type: string;
  required: boolean;
  fields: ScriptApiField[];
}

export interface ScriptApiMethod {
  name: string;
  signature: string;
  returnType: string;
  parameters: ScriptApiParameter[];
  example: string;
}

export interface ScriptApiService {
  beanName: string;
  className: string;
  methods: ScriptApiMethod[];
}
