import request from '@/api/request';
import type { PageData, PageForm, Result } from '@/types/api';

export type FlowState = 'APPROVING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';
export const stateLabels: Record<FlowState, string> = {
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  WITHDRAWN: '已撤回',
};
export type TaskBox = 'PENDING' | 'COMPLETED' | 'STARTED';
export interface FlowTask {
  id: string;
  nodeCode: string;
  name: string;
  candidates: string[];
}
export interface ApprovalDetail {
  id: string;
  businessType: string;
  businessId: string;
  number: string;
  canWithdraw: boolean;
  currentTaskId?: string;
  actorNames: Record<string, string>;
  run: {
    id: string;
    applicantId: string;
    state: FlowState;
    tasks: FlowTask[];
    history: {
      id: string;
      nodeName: string;
      actorId?: string;
      action: string;
      opinion?: string;
      time: string;
    }[];
  };
  candidateChanges: {
    id: string;
    taskId: string;
    operatorId: string;
    beforeCandidates: string[];
    afterCandidates: string[];
    reason: string;
    time: string;
  }[];
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

const root = '/workflow/process';
export const flowPost = <T>(path: string, data: unknown) =>
  request.post<Result<T>>(`${root}/${path}`, data).then((response) => response.data.data);
export const flowGet = <T>(path: string) =>
  request.get<Result<T>>(`${root}/${path}`).then((response) => response.data.data);
export const workflowApi = {
  tasks: (form: PageForm & { box: TaskBox }) => flowPost<PageData<TaskRow>>('task/listPage', form),
  detail: (id: string) => flowPost<ApprovalDetail>('instance/detail', { id }),
  count: () => flowGet<number>('task/count'),
  approve: (data: {
    instanceId: string;
    taskId: string;
    action: string;
    opinion?: string;
    requestId: string;
  }) => flowPost('runtime/approve', data),
  withdraw: (id: string, requestId: string) => flowPost('runtime/withdraw', { id, requestId }),
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
