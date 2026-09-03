import { defineAccessResource } from '@/domain/common/page/access/access';

export const featureAccess = defineAccessResource('sys:base:feature', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
});
