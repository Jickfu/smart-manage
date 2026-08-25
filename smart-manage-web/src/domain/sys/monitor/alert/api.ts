import request from '@/api/request';
import type { PageData, Result } from '@/types/api';
import type { AlertIncident, AlertRule, AlertRuleSave } from './types';
export const monitorAlertApi = {
  rules: () =>
    request.get<Result<AlertRule[]>>('/sys/monitor/alert/rules').then((r) => r.data.data),
  saveRule: (data: AlertRuleSave) =>
    request.put<Result<void>>('/sys/monitor/alert/rules', data).then((r) => r.data.data),
  incidents: (params: { pageNum: number; pageSize: number; status?: string }) =>
    request
      .get<Result<PageData<AlertIncident>>>('/sys/monitor/alert/incidents', { params })
      .then((r) => r.data.data),
};
