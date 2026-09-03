import { defineAccessResource } from '@/domain/common/page/access/access';

export const uiConfigAccess = defineAccessResource('sys:base:ui-config', {
  detail: 'detail',
  save: 'save',
});
