import { defineAccessResource } from '@/domain/common/page/access';

export const userAccess = defineAccessResource('sys:base:user', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
  assignRoles: 'assignRoles',
  resetPassword: 'resetPassword',
  temporaryLogin: 'temporaryLogin',
  readSensitive: 'sensitive:read',
});
