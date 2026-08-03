import { defineAccessResource } from '@/domain/common/page/access';

export const menuAccess = defineAccessResource('sys:base:menu', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
});
