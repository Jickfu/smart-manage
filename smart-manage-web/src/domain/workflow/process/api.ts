import request from '@/api/request';
import type { PageData, PageForm, Result } from '@/types/api';

export type FlowState = 'APPROVING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN' | 'TERMINATED';
export const stateLabels: Record<FlowState, string> = {
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  WITHDRAWN: '已撤回',
  TERMINATED: '已终止',
};
export const workflowActionLabels: Record<string, string> = {
  SUBMITTED: '提交',
  APPROVED: '同意',
  REJECTED: '拒绝',
  WITHDRAWN: '撤回',
  TERMINATED: '终止',
  SCRIPT: '脚本执行',
  TASK_BACK: '拿回',
  TRANSFER: '转办',
  DELEGATE: '委派',
  ADD_SIGN: '加签',
  REDUCE_SIGN: '减签',
  ADMIN_JUMP: '管理员跳转',
  ADMIN_RETURN: '管理员退回',
  SUSPEND: '挂起',
  RESUME: '恢复',
  VARIABLES_UPDATE: '修改流程变量',
  SCRIPT_RETRY: '重试脚本节点',
  SCRIPT_SUCCESS: '脚本执行成功',
  SCRIPT_ERROR: '脚本执行失败',
  SCRIPT_TIMEOUT: '脚本执行超时',
};
export type TaskBox = 'PENDING' | 'COMPLETED' | 'STARTED';
export interface FlowTask {
  id: string;
  nodeCode: string;
  name: string;
  candidates: string[];
  script: boolean;
}
export interface ApprovalDetail {
  id: string;
  businessType: string;
  businessId: string;
  number: string;
  canWithdraw: boolean;
  canTakeBack: boolean;
  currentTaskId?: string;
  actorNames: Record<string, string>;
  run: {
    id: string;
    applicantId: string;
    state: FlowState;
    active: boolean;
    variables: Record<string, unknown>;
    tasks: FlowTask[];
    nodeTargets: { code: string; name: string; script: boolean }[];
    history: {
      id: string;
      nodeCode: string;
      nodeName: string;
      actorId?: string;
      action: string;
      opinion?: string;
      time: string;
    }[];
  };
}
export interface TaskRow {
  id: string;
  businessType: string;
  businessId: string;
  number: string;
  state: FlowState;
  currentNode: string;
}
export interface DefinitionVersion {
  id: string;
  code: string;
  name: string;
  version: string;
  published: boolean;
}
export interface WorkflowBusinessChoice {
  key: string;
  name: string;
  featureKey: string;
  domainId: string;
  domainName: string;
  appId: string;
  appName: string;
}
export interface DefinitionRow {
  id: string;
  number: string;
  name: string;
  businessType?: string;
  enabled: boolean;
  version: number;
  definitions: DefinitionVersion[];
}
export interface DefinitionListForm extends PageForm {
  keyword?: string;
  domainId?: string;
  appId?: string;
}
export interface MaintenanceTarget {
  id: string;
  number: string;
  tasks: FlowTask[];
}

export interface InstanceRow {
  id: string;
  businessType: string;
  businessId: string;
  number: string;
  orgId: string;
  applicantId: string;
  applicantName?: string;
  state: FlowState;
  active: boolean;
  currentNode: string;
  createTime: string;
}

export interface WorkflowOperation {
  id: string;
  taskId?: string;
  action: string;
  operatorId: string;
  reason: string;
  time: string;
}

export interface CandidateChange {
  id: string;
  taskId: string;
  operatorId: string;
  beforeCandidates: string[];
  afterCandidates: string[];
  reason: string;
  time: string;
}

export interface InstanceAdminDetail extends Omit<
  ApprovalDetail,
  'canWithdraw' | 'canTakeBack' | 'currentTaskId'
> {
  orgId: string;
  applicantId: string;
  candidateChanges: CandidateChange[];
  operations: WorkflowOperation[];
}

export interface TaskMonitorRow {
  id: string;
  instanceId: string;
  number: string;
  businessType: string;
  nodeCode: string;
  nodeName: string;
  active: boolean;
  candidates: string[];
  candidateNames: Record<string, string>;
  createTime: string;
}

export interface FlowLogRow {
  id: string;
  instanceId: string;
  taskId?: string;
  number: string;
  businessType: string;
  nodeName?: string;
  action: string;
  operatorId?: string;
  operatorName?: string;
  opinion?: string;
  eventTime?: string;
  source: 'ENGINE' | 'MANAGEMENT' | 'SCRIPT';
}

const root = '/workflow/process';
export const flowPost = <T>(path: string, data: unknown) =>
  request.post<Result<T>>(`${root}/${path}`, data).then((response) => response.data.data);
export const flowGet = <T>(path: string) =>
  request.get<Result<T>>(`${root}/${path}`).then((response) => response.data.data);
export const workflowApi = {
  tasks: (form: PageForm & { box: TaskBox }) => flowPost<PageData<TaskRow>>('task/listPage', form),
  detail: (id: string) => flowPost<ApprovalDetail>('instance/detail', { id }),
  instanceList: (form: PageForm & { keyword?: string }) =>
    flowPost<PageData<InstanceRow>>('instance/management/listPage', form),
  instanceAdminDetail: (id: string) =>
    flowPost<InstanceAdminDetail>('instance/management/detail', { id }),
  taskMonitor: (form: PageForm & { keyword?: string }) =>
    flowPost<PageData<TaskMonitorRow>>('task/monitor/listPage', form),
  flowLogs: (form: PageForm & { keyword?: string }) =>
    flowPost<PageData<FlowLogRow>>('flow-log/listPage', form),
  count: () => flowGet<number>('task/count'),
  approve: (data: {
    instanceId: string;
    taskId: string;
    action: string;
    opinion?: string;
    requestId: string;
  }) => flowPost('runtime/approve', data),
  withdraw: (id: string, requestId: string) => flowPost('runtime/withdraw', { id, requestId }),
  cooperate: (data: {
    instanceId: string;
    taskId: string;
    action: 'TRANSFER' | 'DELEGATE' | 'ADD_SIGN' | 'REDUCE_SIGN';
    targetUserIds: string[];
    reason: string;
    requestId: string;
  }) => flowPost('runtime/cooperate', data),
  takeBack: (instanceId: string, reason: string, requestId: string) =>
    flowPost('runtime/take-back', { instanceId, reason, requestId }),
  definitions: (form: DefinitionListForm) =>
    flowPost<PageData<DefinitionRow>>('definition/listPage', form),
  versions: (id: string) => flowGet<DefinitionRow>(`definition/versions/${id}`),
  businessTypes: () => flowGet<WorkflowBusinessChoice[]>('definition/business-types'),
  design: (id: string) =>
    flowGet<{
      definition: { flowName: string; version: string; isPublish: number };
      digest: string;
    }>(`definition/design/${id}`),
};
