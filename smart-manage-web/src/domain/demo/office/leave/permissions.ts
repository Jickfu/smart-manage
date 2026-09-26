import { defineAccessResource } from '@/domain/common/page/access/access';
export const leaveAccess = defineAccessResource('demo:office:leave', {
  list: 'listPage',
  detail: 'detail',
  save: 'save',
  submit: 'submit',
  delete: 'delete',
});
