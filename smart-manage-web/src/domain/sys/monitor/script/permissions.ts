import { defineAccessResource } from '@/domain/common/page/access';

export const scriptAccess = defineAccessResource('sys:monitor:script', {
  execute: 'execute',
  listPage: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  logList: 'log:listPage',
  logDetail: 'log:detail',
});
