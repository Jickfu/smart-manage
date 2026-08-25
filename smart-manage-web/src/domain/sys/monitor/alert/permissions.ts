import { defineAccessResource } from '@/domain/common/page/access';
export const monitorAlertAccess = defineAccessResource('sys:monitor:alert', {
  view: 'view',
  manage: 'manage',
});
