import { defineAccessResource } from '@/domain/common/page/access';
export const accountAccess = defineAccessResource('sys:message:email-account', {
  listPage: 'listPage',
  detail: 'detail',
  save: 'save',
  enable: 'enable',
  delete: 'delete',
  test: 'test',
});
export const composeAccess = defineAccessResource('sys:message:email-compose', {
  save: 'send',
  send: 'send',
});
export const recordAccess = defineAccessResource('sys:message:email-record', {
  listPage: 'listPage',
  detail: 'detail',
  save: 'retry',
  delete: 'cancel',
  retry: 'retry',
  cancel: 'cancel',
});
