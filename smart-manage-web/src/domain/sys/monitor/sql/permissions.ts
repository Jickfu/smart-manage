import { defineAccessResource } from '@/domain/common/page/access/access';

export const sqlAccess = defineAccessResource('sys:monitor:sql', {
  execute: 'execute',
  listPage: 'log:listPage',
  detail: 'log:detail',
});
