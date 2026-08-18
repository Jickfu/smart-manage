import request from '@/api/request';
import type { Result } from '@/types/api';

export interface PinnedAppVO {
  number: string;
  name: string;
  seq: number;
}

export function fetchPinnedApps() {
  return request
    .get<Result<PinnedAppVO[]>>('/sys/base/user/current/app-pins')
    .then((response) => response.data.data);
}

export function pinApp(appNumber: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/app-pins/pin', { appNumber })
    .then((response) => response.data);
}

export function unpinApp(appNumber: string) {
  return request
    .post<Result<void>>('/sys/base/user/current/app-pins/unpin', { appNumber })
    .then((response) => response.data);
}
