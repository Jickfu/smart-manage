import { defineAccessResource } from '@/domain/common/page/access/access';

export const basicDataAccess = defineAccessResource('sys:base:basic-data', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
  enable: 'enable',
  disable: 'disable',
});
