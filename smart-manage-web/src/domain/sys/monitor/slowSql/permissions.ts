import { defineAccessResource } from '@/domain/common/page/access/access';

export const slowSqlAccess = defineAccessResource('sys:monitor:slow-sql', {
  access: 'access',
  config: 'config',
  clear: 'clear',
});
