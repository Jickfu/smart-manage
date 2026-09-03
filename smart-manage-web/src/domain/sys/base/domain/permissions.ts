import { defineAccessResource } from '@/domain/common/page/access/access';

export const domainAccess = defineAccessResource('sys:base:domain', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
});
