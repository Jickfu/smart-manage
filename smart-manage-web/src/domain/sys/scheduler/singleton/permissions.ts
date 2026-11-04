import { defineAccessResource } from '@/domain/common/page/access/access';
export const singletonAccess = defineAccessResource('sys:scheduler:singleton', {
  list: 'listPage',
  detail: 'detail',
  save: 'control',
});
