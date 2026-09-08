import { defineAccessResource } from '@/domain/common/page/access/access';

export const weakPasswordAccess = defineAccessResource('sys:base:weak-password', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  delete: 'delete',
});
