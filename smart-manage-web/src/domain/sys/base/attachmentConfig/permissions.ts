import { defineAccessResource } from '@/domain/common/page/access';

export const attachmentConfigAccess = defineAccessResource('sys:base:attachment-config', {
  detail: 'detail',
  save: 'save',
});
