import { defineAccessResource } from '@/domain/common/page/access';

export const cacheAccess = defineAccessResource('sys:monitor:cache', {
  list: 'listPage',
  clear: 'clear',
  clearAll: 'clearAll',
});
