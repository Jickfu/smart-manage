import request from '@/api/request';
import type { PageData, PageForm, Result } from '@/types/api';
import type { BusinessAttachment } from '@/domain/common/attachment/types';
export interface LeaveDetail {
  id: string;
  version: number;
  number: string;
  clientKey: string;
  orgId: string;
  applicantId: string;
  bizDate: string;
  leaveType: string;
  startTime: string;
  endTime: string;
  days: number;
  reason: string;
  billStatus: 'A' | 'B' | 'C';
  currentInstanceId?: string;
  lastOutcome?: string;
  attachments: BusinessAttachment[];
  retainedAttachmentIds: string[];
}
const post = <T>(path: string, data: unknown) =>
  request
    .post<Result<T>>(`/demo/office/leave/${path}`, data)
    .then((response) => response.data.data);
export const leaveApi = {
  listPage: (form: PageForm) => post<PageData<LeaveDetail>>('listPage', form),
  detail: (id: string) => post<LeaveDetail>('detail', { id }),
  approval: (id: string, instanceId: string) =>
    post<LeaveDetail>('approval-detail', { id, instanceId }),
  save: (form: Record<string, unknown>) => post<string>('save', form),
  submit: (form: Record<string, unknown>) => post<string>('submit', form),
  delete: (id: string, version: number) => post('delete', { id, version }),
};
