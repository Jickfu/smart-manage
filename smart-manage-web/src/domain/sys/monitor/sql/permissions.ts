import { defineAccessResource } from '@/domain/common/page/access/access';

export const sqlAccess = defineAccessResource('sys:monitor:sql', {
  execute: 'execute',
  catalog: 'catalog',
  listPage: 'log:listPage',
  detail: 'log:detail',
});
