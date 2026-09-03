import { defineAccessResource } from '@/domain/common/page/access/access';
export const monitorAlertAccess = defineAccessResource('sys:monitor:alert', {
  view: 'view',
  manage: 'manage',
});
