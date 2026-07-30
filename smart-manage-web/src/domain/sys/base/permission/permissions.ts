import { defineAccessResource } from '@/domain/common/page/access';

export const permissionAccess = defineAccessResource('sys:base:permission', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  select: 'select',
});
