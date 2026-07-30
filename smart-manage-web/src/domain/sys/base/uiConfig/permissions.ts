import { defineAccessResource } from '@/domain/common/page/access';

export const uiConfigAccess = defineAccessResource('sys:base:ui-config', {
  detail: 'detail',
  save: 'save',
});
