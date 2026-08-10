import request from '@/api/request';
import type { Result } from '@/types/api';
import { nodeMonitorApi } from '../node/api';
import type { ThreadCollectForm, ThreadDiagnosticResult } from './types';

export const threadDiagnosticApi = {
  instances: nodeMonitorApi.instances,
  list: (instanceId?: string) =>
    request
      .get<Result<ThreadDiagnosticResult>>('/sys/monitor/thread/list', { params: { instanceId } })
      .then((response) => response.data.data),
  detail: (instanceId: string | undefined, threadId: number) =>
    request
      .get<Result<ThreadDiagnosticResult>>(`/sys/monitor/thread/${threadId}`, {
        params: { instanceId, maxDepth: 128 },
      })
      .then((response) => response.data.data),
  hot: (form: ThreadCollectForm) =>
    request
      .post<Result<ThreadDiagnosticResult>>('/sys/monitor/thread/hot', form)
      .then((response) => response.data.data),
  dump: (form: ThreadCollectForm) =>
    request
      .post<Result<ThreadDiagnosticResult>>('/sys/monitor/thread/dump', form)
      .then((response) => response.data.data),
  deadlocks: (form: ThreadCollectForm) =>
    request
      .post<Result<ThreadDiagnosticResult>>('/sys/monitor/thread/deadlocks', form)
      .then((response) => response.data.data),
};
