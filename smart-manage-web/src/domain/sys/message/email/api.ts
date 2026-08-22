import request from '@/api/request';
import type { Result } from '@/types/api';
import type { AccountForm, AccountOption, EmailAccount, EmailRecord, PageData } from './types';
const post = <T>(url: string, data: unknown) =>
  request.post<Result<T>>(url, data).then((r) => r.data.data);
export const emailApi = {
  accountList: (data: unknown) =>
    post<PageData<EmailAccount>>('/sys/message/email/account/listPage', data),
  accountDetail: (id: string) => post<EmailAccount>('/sys/message/email/account/detail', { id }),
  accountSave: (data: AccountForm) => post<string>('/sys/message/email/account/save', data),
  accountEnable: (data: { id: string; version: number; enabled: boolean }) =>
    post<void>('/sys/message/email/account/enable', data),
  accountDelete: (data: { id: string; version: number }) =>
    post<void>('/sys/message/email/account/delete', data),
  accountTest: (data: { accountId: string; recipient?: string }) =>
    post<string>('/sys/message/email/account/test', data),
  options: () =>
    request
      .get<Result<AccountOption[]>>('/sys/message/email/account/manual-options')
      .then((r) => r.data.data),
  send: (data: unknown) => post<string>('/sys/message/email/compose/send', data),
  recordList: (data: unknown) =>
    post<PageData<EmailRecord>>('/sys/message/email/record/listPage', data),
  recordDetail: (id: string) => post<EmailRecord>('/sys/message/email/record/detail', { id }),
  retry: (id: string) => post<string>('/sys/message/email/record/retry', { id }),
  cancel: (data: { id: string; version: number }) =>
    post<void>('/sys/message/email/record/cancel', data),
};
