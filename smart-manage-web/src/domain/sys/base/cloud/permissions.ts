import { defineAccessResource } from '@/domain/common/page/access';

export const cloudAccess = defineAccessResource('sys:base:cloud', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
});
