import { defineAccessResource } from '@/domain/common/page/access';

export const cacheAccess = defineAccessResource('sys:monitor:cache', {
  save: 'listPage',
  list: 'listPage',
  clear: 'clear',
  clearAll: 'clearAll',
  value: 'value',
  delete: 'delete',
});
