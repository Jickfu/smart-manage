import { defineAccessResource } from '@/domain/common/page/access/access';
export const passwordConfigAccess = defineAccessResource('sys:base:password-config', {
  detail: 'detail',
  save: 'save',
});
