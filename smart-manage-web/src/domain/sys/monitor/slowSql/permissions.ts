import { defineAccessResource } from '@/domain/common/page/access';

export const slowSqlAccess = defineAccessResource('sys:monitor:slow-sql', {
  access: 'access',
  config: 'config',
  clear: 'clear',
});
