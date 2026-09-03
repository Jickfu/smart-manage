import { defineAccessResource } from '@/domain/common/page/access/access';

export const sysParamAccess = defineAccessResource('sys:base:param', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
});
