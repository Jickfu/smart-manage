import { defineAccessResource } from '@/domain/common/page/access/access';

export const fileConfigAccess = defineAccessResource('sys:base:file-config', {
  detail: 'detail',
  save: 'save',
});
