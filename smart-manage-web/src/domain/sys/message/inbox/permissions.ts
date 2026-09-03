import { defineAccessResource } from '@/domain/common/page/access/access';

export const inboxBroadcastAccess = defineAccessResource('sys:message:inbox-broadcast', {
  listPage: 'listPage',
  detail: 'detail',
  save: 'save',
  publish: 'publish',
  retry: 'retry',
});
