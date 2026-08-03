import { defineAccessResource } from '@/domain/common/page/access';
export const redisAccess = defineAccessResource('sys:monitor:redis', {
  list: 'listPage',
  value: 'value',
  delete: 'delete',
});
