import { defineAccessResource } from '@/domain/common/page/access';

export const sqlAccess = defineAccessResource('sys:monitor:sql', {
  execute: 'execute',
  listPage: 'log:listPage',
  detail: 'log:detail',
});
