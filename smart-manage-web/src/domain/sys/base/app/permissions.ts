import { defineAccessResource } from '@/domain/common/page/access/access';

export const appAccess = defineAccessResource('sys:base:app', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
});
