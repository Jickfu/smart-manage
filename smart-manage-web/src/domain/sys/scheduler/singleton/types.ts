export type SingletonState = 'STOPPED' | 'STARTING' | 'RUNNING' | 'STOPPING' | 'FAILED' | 'UNKNOWN';
export interface SingletonTask {
  number: string;
  name: string;
  appNumber: string;
  description: string;
  controllable: boolean;
  available: boolean;
  requested: boolean;
  version: number;
  state: SingletonState;
  ownerInstanceId?: string;
  runId?: string;
  generation: string;
  startedAt?: string;
  heartbeatAt?: string;
  retryAt?: string;
  lastError?: string;
}
export interface SingletonCommand {
  number: string;
  version: number;
  requested: boolean;
}
export interface SingletonEvent {
  id: string;
  occurredAt: string;
  eventType: string;
  instanceId?: string;
  runId?: string;
  reason?: string;
}
export const singletonStateLabels: Record<SingletonState, string> = {
  STOPPED: '已停止',
  STARTING: '启动中',
  RUNNING: '运行中',
  STOPPING: '停止中',
  FAILED: '失败退避',
  UNKNOWN: '状态未知',
};
export function canStart(task?: SingletonTask) {
  return Boolean(
    task?.available &&
    task.controllable &&
    !task.requested &&
    !task.ownerInstanceId &&
    ['STOPPED', 'FAILED'].includes(task.state),
  );
}
export function canStop(task?: SingletonTask) {
  return Boolean(task?.available && task.controllable && task.requested);
}
