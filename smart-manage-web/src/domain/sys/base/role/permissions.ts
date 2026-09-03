import { defineAccessResource } from '@/domain/common/page/access/access';

export const roleAccess = defineAccessResource('sys:base:role', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  assignPermissions: 'assignPermissions',
  assignDataScopes: 'assignDataScopes',
});
