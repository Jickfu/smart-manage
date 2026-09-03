import { defineAccessResource } from '@/domain/common/page/access/access';

export const jobAccess = defineAccessResource('sys:scheduler:job', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
});
